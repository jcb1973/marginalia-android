package com.jcb1973.marginalia.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag

@Composable
fun ImageCropOverlay(
    onCropRectChanged: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    var startOffset by remember { mutableStateOf(Offset.Zero) }
    var endOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .testTag("imageCropOverlay")
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        startOffset = offset
                        endOffset = offset
                        isDragging = true
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        endOffset = change.position
                    },
                    onDragEnd = {
                        isDragging = false
                        val rect = Rect(
                            left = minOf(startOffset.x, endOffset.x),
                            top = minOf(startOffset.y, endOffset.y),
                            right = maxOf(startOffset.x, endOffset.x),
                            bottom = maxOf(startOffset.y, endOffset.y)
                        )
                        onCropRectChanged(rect)
                    }
                )
            }
    ) {
        if (isDragging || (startOffset != Offset.Zero && endOffset != Offset.Zero)) {
            val left = minOf(startOffset.x, endOffset.x)
            val top = minOf(startOffset.y, endOffset.y)
            val width = maxOf(startOffset.x, endOffset.x) - left
            val height = maxOf(startOffset.y, endOffset.y) - top

            // Dim overlay outside crop
            drawRect(
                color = Color.Black.copy(alpha = 0.4f),
                topLeft = Offset.Zero,
                size = this.size
            )
            // Clear the crop area
            drawRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(width, height),
                blendMode = androidx.compose.ui.graphics.BlendMode.Clear
            )
            // Dashed border
            drawRect(
                color = Color.White,
                topLeft = Offset(left, top),
                size = Size(width, height),
                style = Stroke(
                    width = 2f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            )
        }
    }
}
