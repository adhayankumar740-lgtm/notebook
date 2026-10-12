package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Handwriting / typed text drawn on top of a PDF page or photo.
 * All coordinates are normalized (0..1) relative to the page, so they stay
 * correct at any zoom level or render resolution.
 */
sealed class PageAnnotation {
    abstract val page: Int
    abstract val color: Int
}

data class StrokeAnnotation(
    override val page: Int,
    override val color: Int,
    val widthFraction: Float,          // stroke width as fraction of page width
    val points: List<Pair<Float, Float>>
) : PageAnnotation()

data class TextAnnotation(
    override val page: Int,
    override val color: Int,
    val sizeFraction: Float,           // text size as fraction of page width
    val x: Float,                      // top-left, normalized
    val y: Float,
    val text: String
) : PageAnnotation()

object AnnotationStore {
    private const val TAG = "AnnotationStore"

    private fun storeFile(context: Context, internalPath: String): File {
        val dir = File(context.filesDir, "annotations")
        dir.mkdirs()
        return File(dir, internalPath.replace(Regex("[^a-zA-Z0-9._-]"), "_") + ".json")
    }

    fun load(context: Context, internalPath: String): List<PageAnnotation> {
        return try {
            val f = storeFile(context, internalPath)
            if (!f.exists()) return emptyList()
            val arr = JSONArray(f.readText())
            val out = mutableListOf<PageAnnotation>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                if (o.getString("t") == "s") {
                    val pts = o.getJSONArray("pts")
                    val list = mutableListOf<Pair<Float, Float>>()
                    var k = 0
                    while (k + 1 < pts.length()) {
                        list.add(pts.getDouble(k).toFloat() to pts.getDouble(k + 1).toFloat())
                        k += 2
                    }
                    out.add(StrokeAnnotation(o.getInt("p"), o.getInt("c"), o.getDouble("w").toFloat(), list))
                } else {
                    out.add(
                        TextAnnotation(
                            o.getInt("p"), o.getInt("c"), o.getDouble("s").toFloat(),
                            o.getDouble("x").toFloat(), o.getDouble("y").toFloat(), o.getString("txt")
                        )
                    )
                }
            }
            out
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load annotations", e)
            emptyList()
        }
    }

    fun save(context: Context, internalPath: String, items: List<PageAnnotation>) {
        try {
            val f = storeFile(context, internalPath)
            if (items.isEmpty()) {
                if (f.exists()) f.delete()
                return
            }
            val arr = JSONArray()
            for (a in items) {
                val o = JSONObject()
                when (a) {
                    is StrokeAnnotation -> {
                        o.put("t", "s"); o.put("p", a.page); o.put("c", a.color); o.put("w", a.widthFraction.toDouble())
                        val pts = JSONArray()
                        for ((x, y) in a.points) { pts.put(x.toDouble()); pts.put(y.toDouble()) }
                        o.put("pts", pts)
                    }
                    is TextAnnotation -> {
                        o.put("t", "t"); o.put("p", a.page); o.put("c", a.color); o.put("s", a.sizeFraction.toDouble())
                        o.put("x", a.x.toDouble()); o.put("y", a.y.toDouble()); o.put("txt", a.text)
                    }
                }
                arr.put(o)
            }
            f.writeText(arr.toString())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save annotations", e)
        }
    }

    fun delete(context: Context, internalPath: String) {
        try {
            val f = storeFile(context, internalPath)
            if (f.exists()) f.delete()
        } catch (_: Exception) {}
    }

    /** Draws the given (single page) annotations onto a canvas of size [width] x [height]. */
    fun draw(canvas: Canvas, width: Float, height: Float, items: List<PageAnnotation>) {
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        for (a in items) {
            when (a) {
                is StrokeAnnotation -> {
                    if (a.points.isEmpty()) continue
                    strokePaint.color = a.color
                    strokePaint.strokeWidth = (a.widthFraction * width).coerceAtLeast(1f)
                    if (a.points.size == 1) {
                        val (x, y) = a.points[0]
                        canvas.drawPoint(x * width, y * height, strokePaint)
                    } else {
                        val path = Path()
                        path.moveTo(a.points[0].first * width, a.points[0].second * height)
                        for (i in 1 until a.points.size) {
                            path.lineTo(a.points[i].first * width, a.points[i].second * height)
                        }
                        canvas.drawPath(path, strokePaint)
                    }
                }
                is TextAnnotation -> {
                    textPaint.color = a.color
                    textPaint.textSize = a.sizeFraction * width
                    var baseline = a.y * height + textPaint.textSize
                    for (line in a.text.split("\n")) {
                        canvas.drawText(line, a.x * width, baseline, textPaint)
                        baseline += textPaint.textSize * 1.2f
                    }
                }
            }
        }
    }

    /** Returns the annotation closest to the touch point (pixels), or null if nothing is near. */
    fun hitTest(items: List<PageAnnotation>, px: Float, py: Float, width: Float, height: Float, slop: Float): PageAnnotation? {
        val measure = Paint(Paint.ANTI_ALIAS_FLAG)
        var best: PageAnnotation? = null
        var bestDist = Float.MAX_VALUE
        for (a in items) {
            val d: Float = when (a) {
                is StrokeAnnotation -> {
                    var m = Float.MAX_VALUE
                    for ((x, y) in a.points) {
                        val dx = x * width - px
                        val dy = y * height - py
                        val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                        if (dist < m) m = dist
                    }
                    m - a.widthFraction * width / 2f
                }
                is TextAnnotation -> {
                    measure.textSize = a.sizeFraction * width
                    val lines = a.text.split("\n")
                    val w = (lines.maxOfOrNull { measure.measureText(it) } ?: 0f)
                    val h = measure.textSize * 1.2f * lines.size
                    val left = a.x * width
                    val top = a.y * height
                    val dx = maxOf(left - px, 0f, px - (left + w))
                    val dy = maxOf(top - py, 0f, py - (top + h))
                    Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
                }
            }
            if (d < bestDist) { bestDist = d; best = a }
        }
        return if (bestDist <= slop) best else null
    }
}
