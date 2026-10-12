package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.example.util.AnnotationStore
import com.example.util.PageAnnotation
import com.example.util.StrokeAnnotation

enum class AnnotTool { PEN, TEXT, ERASER }

/**
 * Transparent layer that sits exactly on top of a page image. It always draws the saved
 * annotations of [pageIndex]; when [editing] is true it also accepts pen / text / eraser input.
 */
@Composable
fun AnnotationLayer(
    pageIndex: Int,
    annotations: List<PageAnnotation>,
    editing: Boolean,
    tool: AnnotTool,
    penColor: Int,
    penWidthFraction: Float,
    onAdd: (PageAnnotation) -> Unit,
    onRemove: (PageAnnotation) -> Unit,
    onRequestText: (x: Float, y: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val live = remember(pageIndex) { mutableStateListOf<Offset>() }
    val currentAnnotations by rememberUpdatedState(annotations)
    val onAddState by rememberUpdatedState(onAdd)
    val onRemoveState by rememberUpdatedState(onRemove)
    val onTextState by rememberUpdatedState(onRequestText)

    val pageItems = annotations.filter { it.page == pageIndex }

    val inputModifier = if (editing) {
        Modifier.pointerInput(pageIndex, tool, penColor, penWidthFraction) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val w = size.width.toFloat().coerceAtLeast(1f)
                val h = size.height.toFloat().coerceAtLeast(1f)
                when (tool) {
                    AnnotTool.PEN -> {
                        live.clear()
                        live.add(down.position)
                        down.consume()
                        var cancelled = false
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.changes.size > 1) { cancelled = true; break } // pinch-zoom, not drawing
                            val ch = event.changes.firstOrNull() ?: break
                            if (ch.pressed) {
                                live.add(ch.position)
                                ch.consume()
                            } else {
                                ch.consume()
                                break
                            }
                        }
                        if (!cancelled && live.isNotEmpty()) {
                            onAddState(
                                StrokeAnnotation(
                                    page = pageIndex,
                                    color = penColor,
                                    widthFraction = penWidthFraction,
                                    points = live.map { (it.x / w).coerceIn(0f, 1f) to (it.y / h).coerceIn(0f, 1f) }
                                )
                            )
                        }
                        live.clear()
                    }
                    AnnotTool.TEXT -> {
                        val up = waitForUpOrCancellation()
                        if (up != null) {
                            up.consume()
                            onTextState((up.position.x / w).coerceIn(0f, 1f), (up.position.y / h).coerceIn(0f, 1f))
                        }
                    }
                    AnnotTool.ERASER -> {
                        val up = waitForUpOrCancellation()
                        if (up != null) {
                            up.consume()
                            val hit = AnnotationStore.hitTest(
                                currentAnnotations.filter { it.page == pageIndex },
                                up.position.x, up.position.y, w, h, slop = w * 0.04f
                            )
                            if (hit != null) onRemoveState(hit)
                        }
                    }
                }
            }
        }
    } else Modifier

    Canvas(
        modifier = modifier
            .onSizeChanged { size = it }
            .then(inputModifier)
    ) {
        drawIntoCanvas { c ->
            AnnotationStore.draw(c.nativeCanvas, this.size.width, this.size.height, pageItems)
            if (live.isNotEmpty()) {
                val w = this.size.width
                val h = this.size.height
                val temp = StrokeAnnotation(
                    page = pageIndex,
                    color = penColor,
                    widthFraction = penWidthFraction,
                    points = live.map { it.x / w to it.y / h }
                )
                AnnotationStore.draw(c.nativeCanvas, w, h, listOf(temp))
            }
        }
    }
}
