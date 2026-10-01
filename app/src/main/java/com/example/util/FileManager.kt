package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.model.DocumentFile
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.DecimalFormat

object FileManager {
    private const val TAG = "FileManager"

    data class ImportedFileInfo(
        val originalFileName: String,
        val relativeInternalPath: String,
        val fileType: String,
        val mimeType: String,
        val fileSizeBytes: Long,
        val pageCount: Int,
        val thumbnailPath: String?
    )

    /**
     * Resolves the actual File object on internal storage from a relative path.
     */
    fun getInternalFile(context: Context, relativePath: String): File {
        return File(context.filesDir, relativePath)
    }

    /**
     * Copies a selected document/photo Uri into the app's internal phone storage
     * organized by Subject and Module folders.
     */
    fun importFileFromUri(
        context: Context,
        uri: Uri,
        subjectId: Long,
        moduleId: Long
    ): ImportedFileInfo? {
        return try {
            val contentResolver = context.contentResolver
            var displayName = "document_${System.currentTimeMillis()}"
            var fileSize = 0L

            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) {
                        val name = cursor.getString(nameIndex)
                        if (!name.isNullOrBlank()) displayName = name
                    }
                    if (sizeIndex != -1) {
                        fileSize = cursor.getLong(sizeIndex)
                    }
                }
            }

            val rawMimeType = contentResolver.getType(uri) ?: getMimeTypeFromFileName(displayName)
            val fileType = when {
                rawMimeType.contains("pdf", ignoreCase = true) || displayName.endsWith(".pdf", ignoreCase = true) -> "PDF"
                rawMimeType.startsWith("image/", ignoreCase = true) ||
                        displayName.endsWith(".jpg", ignoreCase = true) ||
                        displayName.endsWith(".jpeg", ignoreCase = true) ||
                        displayName.endsWith(".png", ignoreCase = true) ||
                        displayName.endsWith(".webp", ignoreCase = true) -> "IMAGE"
                else -> "OTHER"
            }

            // Create target folder in phone storage: documents/sub_X/mod_Y/
            val documentsDir = File(context.filesDir, "documents/sub_${subjectId}/mod_${moduleId}")
            documentsDir.mkdirs()

            val sanitizedName = displayName.replace("[^a-zA-Z0-9._-]".toRegex(), "_")
            val targetFileName = "${System.currentTimeMillis()}_$sanitizedName"
            val targetFile = File(documentsDir, targetFileName)

            // Copy stream into internal storage
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            val actualFileSize = if (fileSize > 0) fileSize else targetFile.length()
            val relativePath = "documents/sub_${subjectId}/mod_${moduleId}/$targetFileName"

            // Compute page count and thumbnail
            var pageCount = 1
            var thumbnailRelativePath: String? = null

            if (fileType == "PDF") {
                pageCount = PdfHelper.getPageCount(targetFile).coerceAtLeast(1)
                val thumbsDir = File(context.filesDir, "thumbnails")
                thumbsDir.mkdirs()
                val thumbFile = File(thumbsDir, "thumb_${System.currentTimeMillis()}.jpg")
                if (PdfHelper.renderThumbnail(targetFile, thumbFile)) {
                    thumbnailRelativePath = "thumbnails/${thumbFile.name}"
                }
            } else if (fileType == "IMAGE") {
                thumbnailRelativePath = relativePath
            }

            ImportedFileInfo(
                originalFileName = displayName,
                relativeInternalPath = relativePath,
                fileType = fileType,
                mimeType = rawMimeType,
                fileSizeBytes = actualFileSize,
                pageCount = pageCount,
                thumbnailPath = thumbnailRelativePath
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error importing file from Uri", e)
            null
        }
    }

    /**
     * Deletes the document file and its thumbnail from internal storage.
     */
    fun deleteInternalFile(context: Context, relativePath: String, thumbnailPath: String?) {
        try {
            val file = File(context.filesDir, relativePath)
            if (file.exists()) file.delete()

            if (!thumbnailPath.isNullOrBlank() && thumbnailPath != relativePath) {
                val thumb = File(context.filesDir, thumbnailPath)
                if (thumb.exists()) thumb.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete file $relativePath", e)
        }
    }

    /**
     * Opens the document in an external reader app (e.g. Acrobat, Drive PDF, Gallery) via FileProvider.
     */
    fun openFileWithExternalApp(context: Context, document: DocumentFile) {
        try {
            val file = getInternalFile(context, document.internalFilePath)
            if (!file.exists()) {
                Log.e(TAG, "File not found: ${file.absolutePath}")
                return
            }
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, document.mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open with..."))
        } catch (e: Exception) {
            Log.e(TAG, "Could not open external app", e)
        }
    }

    /**
     * Shares the document file via Android share sheet.
     */
    fun shareDocument(context: Context, document: DocumentFile) {
        try {
            val file = getInternalFile(context, document.internalFilePath)
            if (!file.exists()) return

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = document.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, document.title)
                putExtra(Intent.EXTRA_TEXT, "${document.title} (${document.topic})")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Share Document").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            Log.e(TAG, "Could not share document", e)
        }
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
        return DecimalFormat("#,##0.#").format(bytes / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }

    private fun getMimeTypeFromFileName(fileName: String): String {
        val lower = fileName.lowercase()
        return when {
            lower.endsWith(".pdf") -> "application/pdf"
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".webp") -> "image/webp"
            lower.endsWith(".gif") -> "image/gif"
            lower.endsWith(".doc") || lower.endsWith(".docx") -> "application/msword"
            lower.endsWith(".txt") -> "text/plain"
            else -> "*/*"
        }
    }
}
