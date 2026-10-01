package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "documents",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Module::class,
            parentColumns = ["id"],
            childColumns = ["moduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["subjectId"]),
        Index(value = ["moduleId"]),
        Index(value = ["lectureDateMillis"]),
        Index(value = ["fileType"])
    ]
)
data class DocumentFile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val subjectId: Long,
    val moduleId: Long,
    val title: String,
    val originalFileName: String,
    val internalFilePath: String, // Relative to context.filesDir
    val fileType: String = "PDF", // "PDF", "IMAGE", "OTHER"
    val mimeType: String = "application/pdf",
    val fileSizeBytes: Long = 0L,
    val pageCount: Int = 1,
    val thumbnailPath: String? = null,
    val lectureDateMillis: Long? = null,
    val lectureDateString: String = "",
    val lectureTimeString: String = "",
    val lecturerName: String = "",
    val topic: String = "",
    val notes: String = "",
    val tags: String = "", // Comma-separated tags
    val isStarred: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
