package com.example.util

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.File
import java.io.FileOutputStream

object PdfHelper {
    private const val TAG = "PdfHelper"

    /**
     * Inspects a PDF file and returns its total page count.
     */
    fun getPageCount(file: File): Int {
        if (!file.exists() || file.length() == 0L) return 0
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        return try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            renderer.pageCount
        } catch (e: Exception) {
            Log.e(TAG, "Error reading page count for ${file.name}", e)
            1
        } finally {
            try {
                renderer?.close()
                pfd?.close()
            } catch (_: Exception) {}
        }
    }

    /**
     * Renders a specific page of a PDF file to a Bitmap.
     */
    fun renderPageToBitmap(file: File, pageIndex: Int, maxDimension: Int = 1400): Bitmap? {
        if (!file.exists() || file.length() == 0L) return null
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        var page: PdfRenderer.Page? = null
        return try {
            pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd)
            if (pageIndex < 0 || pageIndex >= renderer.pageCount) return null
            
            page = renderer.openPage(pageIndex)
            val origWidth = page.width
            val origHeight = page.height
            
            val scale = (maxDimension.toFloat() / maxOf(origWidth, origHeight)).coerceAtLeast(1.0f)
            val destWidth = (origWidth * scale).toInt()
            val destHeight = (origHeight * scale).toInt()

            val bitmap = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888)
            // PDF backgrounds are transparent by default in PdfRenderer; fill with pure white
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawColor(Color.WHITE)

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error rendering page $pageIndex of ${file.name}", e)
            null
        } finally {
            try {
                page?.close()
                renderer?.close()
                pfd?.close()
            } catch (_: Exception) {}
        }
    }

    /**
     * Renders the first page to a compressed JPEG thumbnail file for fast loading.
     */
    fun renderThumbnail(pdfFile: File, outputFile: File): Boolean {
        return try {
            val bitmap = renderPageToBitmap(pdfFile, 0, maxDimension = 512) ?: return false
            outputFile.parentFile?.mkdirs()
            FileOutputStream(outputFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            bitmap.recycle()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error rendering thumbnail for ${pdfFile.name}", e)
            false
        }
    }

    /**
     * Creates a real, structured lecture note sample PDF file using native Android PdfDocument API.
     */
    fun createSampleLecturePdf(
        targetFile: File,
        subjectName: String,
        moduleName: String,
        topic: String,
        lecturer: String,
        date: String
    ): Boolean {
        return try {
            targetFile.parentFile?.mkdirs()
            val document = PdfDocument()

            // Page 1: Overview & Theory
            val pageInfo1 = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 points
            val page1 = document.startPage(pageInfo1)
            val canvas1 = page1.canvas
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

            // Header background
            paint.color = Color.rgb(30, 58, 138) // Deep Blue
            canvas1.drawRect(0f, 0f, 595f, 110f, paint)

            // Header text
            paint.color = Color.WHITE
            paint.textSize = 22f
            paint.isFakeBoldText = true
            canvas1.drawText("LECTURE NOTES: $subjectName", 40f, 50f, paint)

            paint.textSize = 14f
            paint.isFakeBoldText = false
            canvas1.drawText("$moduleName  •  $date", 40f, 85f, paint)

            // Metadata card
            paint.color = Color.rgb(241, 245, 249) // Light Slate
            canvas1.drawRoundRect(40f, 130f, 555f, 210f, 12f, 12f, paint)

            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas1.drawText("Topic: $topic", 55f, 160f, paint)
            canvas1.drawText("Lecturer: $lecturer", 55f, 185f, paint)

            // Section 1: Introduction
            paint.color = Color.rgb(30, 58, 138)
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas1.drawText("1. Key Principles & Conceptual Overview", 40f, 250f, paint)

            paint.color = Color.rgb(51, 65, 85)
            paint.textSize = 13f
            paint.isFakeBoldText = false
            val lines = listOf(
                "• Self-balancing binary search tree ensures O(log n) search, insert, and delete.",
                "• Balance Factor (BF) = Height(Left Subtree) - Height(Right Subtree).",
                "• A node is considered strictly balanced if BF ∈ {-1, 0, 1}.",
                "• When BF becomes +2 or -2, appropriate tree rotations are applied:",
                "    - Left-Left (LL) Heavy: Perform a Single Right Rotation.",
                "    - Right-Right (RR) Heavy: Perform a Single Left Rotation.",
                "    - Left-Right (LR) Heavy: Left rotate child, then Right rotate parent.",
                "    - Right-Left (RL) Heavy: Right rotate child, then Left rotate parent."
            )
            var yOffset = 285f
            for (line in lines) {
                canvas1.drawText(line, 45f, yOffset, paint)
                yOffset += 26f
            }

            // Box for formulas
            paint.color = Color.rgb(238, 242, 255) // Indigo tint
            canvas1.drawRoundRect(40f, 520f, 555f, 650f, 10f, 10f, paint)
            paint.color = Color.rgb(67, 56, 202)
            paint.textSize = 14f
            paint.isFakeBoldText = true
            canvas1.drawText("CORE COMPLEXITY FORMULAS & INVARIANTS", 55f, 550f, paint)

            paint.color = Color.rgb(30, 41, 59)
            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas1.drawText("Max Height for N nodes:  h < 1.4404 * log2(N + 2) - 0.328", 55f, 580f, paint)
            canvas1.drawText("Lookup Time:  O(log N) worst-case (guaranteed strict bound)", 55f, 605f, paint)
            canvas1.drawText("Space Complexity: O(N) auxiliary memory for balance pointers", 55f, 630f, paint)

            // Footer
            paint.color = Color.GRAY
            paint.textSize = 11f
            canvas1.drawText("DocVault Study Organizer  •  Page 1 of 2", 210f, 810f, paint)

            document.finishPage(page1)

            // Page 2: Examples & Discussion
            val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
            val page2 = document.startPage(pageInfo2)
            val canvas2 = page2.canvas

            // Header background
            paint.color = Color.rgb(30, 58, 138)
            canvas2.drawRect(0f, 0f, 595f, 80f, paint)

            paint.color = Color.WHITE
            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas2.drawText("$subjectName — Continued Notes & Rotations", 40f, 48f, paint)

            // Section 2
            paint.color = Color.rgb(30, 58, 138)
            paint.textSize = 16f
            canvas2.drawText("2. Step-by-Step Rebalancing Procedure", 40f, 130f, paint)

            paint.color = Color.rgb(51, 65, 85)
            paint.textSize = 13f
            paint.isFakeBoldText = false
            val lines2 = listOf(
                "1. Perform standard BST node insertion as usual.",
                "2. Retrace path back to the root updating heights of ancestor nodes.",
                "3. At the first unbalanced node Z, check the balance factor.",
                "4. Identify the pivot node Y and grandchild X causing the imbalance.",
                "5. Execute one of the 4 rotation cases:",
                "   - Case 1 (LL): Rotate Z right. Y becomes new subtree root.",
                "   - Case 2 (RR): Rotate Z left. Y becomes new subtree root.",
                "   - Case 3 (LR): Double rotation. Left on Y, then right on Z.",
                "   - Case 4 (RL): Double rotation. Right on Y, then left on Z.",
                "",
                "Summary: Only O(1) rotation operations required per insert!"
            )
            yOffset = 165f
            for (line in lines2) {
                canvas2.drawText(line, 45f, yOffset, paint)
                yOffset += 26f
            }

            // Footer
            paint.color = Color.GRAY
            paint.textSize = 11f
            canvas2.drawText("DocVault Study Organizer  •  Page 2 of 2", 210f, 810f, paint)

            document.finishPage(page2)

            FileOutputStream(targetFile).use { out ->
                document.writeTo(out)
            }
            document.close()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error generating sample lecture PDF", e)
            false
        }
    }

    /**
     * Generates a sample lecture whiteboard photo PNG.
     */
    fun createSampleWhiteboardImage(targetFile: File, title: String, date: String): Boolean {
        return try {
            targetFile.parentFile?.mkdirs()
            val bitmap = Bitmap.createBitmap(800, 600, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(bitmap)
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)

            // Board background (clean lecture blackboard/dark board style)
            paint.color = Color.rgb(24, 34, 45)
            canvas.drawRect(0f, 0f, 800f, 600f, paint)

            // Frame border
            paint.color = Color.rgb(180, 130, 80)
            paint.style = android.graphics.Paint.Style.STROKE
            paint.strokeWidth = 14f
            canvas.drawRect(7f, 7f, 793f, 593f, paint)

            // White chalk text
            paint.style = android.graphics.Paint.Style.FILL
            paint.color = Color.WHITE
            paint.textSize = 28f
            paint.isFakeBoldText = true
            canvas.drawText("WHITEBOARD: $title", 40f, 65f, paint)

            paint.color = Color.rgb(147, 197, 253) // Light blue chalk
            paint.textSize = 18f
            paint.isFakeBoldText = false
            canvas.drawText("Lecture Date: $date", 40f, 100f, paint)

            // Draw diagram lines (e.g. Tree / Circuit diagram)
            paint.color = Color.rgb(250, 204, 21) // Yellow chalk
            paint.strokeWidth = 4f
            // Root node
            canvas.drawCircle(400f, 180f, 35f, paint)
            paint.color = Color.rgb(24, 34, 45)
            paint.textSize = 20f
            paint.isFakeBoldText = true
            canvas.drawText("50", 387f, 187f, paint)

            // Branch lines
            paint.color = Color.rgb(203, 213, 225)
            paint.strokeWidth = 3f
            canvas.drawLine(375f, 205f, 250f, 280f, paint)
            canvas.drawLine(425f, 205f, 550f, 280f, paint)

            // Left child
            paint.color = Color.rgb(52, 211, 153) // Green chalk
            canvas.drawCircle(250f, 300f, 30f, paint)
            paint.color = Color.rgb(24, 34, 45)
            canvas.drawText("30", 239f, 307f, paint)

            // Right child
            paint.color = Color.rgb(52, 211, 153)
            canvas.drawCircle(550f, 300f, 30f, paint)
            paint.color = Color.rgb(24, 34, 45)
            canvas.drawText("70", 539f, 307f, paint)

            // Grandchildren
            canvas.drawLine(230f, 325f, 160f, 400f, paint)
            canvas.drawLine(270f, 325f, 330f, 400f, paint)

            paint.color = Color.rgb(248, 113, 113) // Red/pink chalk
            canvas.drawCircle(160f, 420f, 26f, paint)
            canvas.drawCircle(330f, 420f, 26f, paint)

            paint.color = Color.rgb(24, 34, 45)
            paint.textSize = 17f
            canvas.drawText("20", 151f, 426f, paint)
            canvas.drawText("40", 321f, 426f, paint)

            // Notes on the side
            paint.color = Color.rgb(226, 232, 240)
            paint.textSize = 16f
            paint.isFakeBoldText = false
            canvas.drawText("• Balance Check: BF(50) = Height(L) - Height(R) = 2 - 1 = +1 (OK)", 50f, 510f, paint)
            canvas.drawText("• Insertion of node 15 will trigger LL rotation!", 50f, 545f, paint)

            FileOutputStream(targetFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error generating sample whiteboard image", e)
            false
        }
    }
}
