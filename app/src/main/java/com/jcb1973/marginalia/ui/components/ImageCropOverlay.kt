package com.jcb1973.marginalia.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

private enum class HandleType {
    NONE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, BODY
}

private fun hitTest(position: Offset, rect: Rect, handleRadius: Float): HandleType {
    val corners = listOf(
        HandleType.TOP_LEFT to Offset(rect.left, rect.top),
        HandleType.TOP_RIGHT to Offset(rect.right, rect.top),
        HandleType.BOTTOM_LEFT to Offset(rect.left, rect.bottom),
        HandleType.BOTTOM_RIGHT to Offset(rect.right, rect.bottom)
    )
    for ((handle, corner) in corners) {
        val dx = position.x - corner.x
        val dy = position.y - corner.y
        if (dx * dx + dy * dy <= handleRadius * handleRadius) {
            return handle
        }
    }
    if (rect.contains(position)) {
        return HandleType.BODY
    }
    return HandleType.NONE
}

private fun computeNewRect(
    handle: HandleType,
    dragStartPointer: Offset,
    dragStartRect: Rect,
    currentPointer: Offset,
    parentBounds: Rect,
    minSize: Float
): Rect {
    return when (handle) {
        HandleType.BODY -> {
            val dx = currentPointer.x - dragStartPointer.x
            val dy = currentPointer.y - dragStartPointer.y
            var newLeft = dragStartRect.left + dx
            var newTop = dragStartRect.top + dy
            // Clamp so rect stays in bounds
            newLeft = newLeft.coerceIn(parentBounds.left, parentBounds.right - dragStartRect.width)
            newTop = newTop.coerceIn(parentBounds.top, parentBounds.bottom - dragStartRect.height)
            Rect(
                left = newLeft,
                top = newTop,
                right = newLeft + dragStartRect.width,
                bottom = newTop + dragStartRect.height
            )
        }
        HandleType.TOP_LEFT -> {
            val newLeft = currentPointer.x
                .coerceIn(parentBounds.left, dragStartRect.right - minSize)
            val newTop = currentPointer.y
                .coerceIn(parentBounds.top, dragStartRect.bottom - minSize)
            Rect(
                left = newLeft,
                top = newTop,
                right = dragStartRect.right,
                bottom = dragStartRect.bottom
            )
        }
        HandleType.TOP_RIGHT -> {
            val newRight = currentPointer.x
                .coerceIn(dragStartRect.left + minSize, parentBounds.right)
            val newTop = currentPointer.y
                .coerceIn(parentBounds.top, dragStartRect.bottom - minSize)
            Rect(
                left = dragStartRect.left,
                top = newTop,
                right = newRight,
                bottom = dragStartRect.bottom
            )
        }
        HandleType.BOTTOM_LEFT -> {
            val newLeft = currentPointer.x
                .coerceIn(parentBounds.left, dragStartRect.right - minSize)
            val newBottom = currentPointer.y
                .coerceIn(dragStartRect.top + minSize, parentBounds.bottom)
            Rect(
                left = newLeft,
                top = dragStartRect.top,
                right = dragStartRect.right,
                bottom = newBottom
            )
        }
        HandleType.BOTTOM_RIGHT -> {
            val newRight = currentPointer.x
                .coerceIn(dragStartRect.left + minSize, parentBounds.right)
            val newBottom = currentPointer.y
                .coerceIn(dragStartRect.top + minSize, parentBounds.bottom)
            Rect(
                left = dragStartRect.left,
                top = dragStartRect.top,
                right = newRight,
                bottom = newBottom
            )
        }
        HandleType.NONE -> dragStartRect
    }
}

@Composable
fun ImageCropOverlay(
    overlaySize: IntSize,
    imageSize: IntSize,
    onCropRectChanged: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val handleRadiusPx = with(density) { 48.dp.toPx() }
    val minSizePx = with(density) { 48.dp.toPx() }
    val handleDrawRadius = with(density) { 8.dp.toPx() }

    // Compute where the image actually renders within the view (ContentScale.Fit)
    val contentBounds = remember(overlaySize, imageSize) {
        val vw = overlaySize.width.toFloat()
        val vh = overlaySize.height.toFloat()
        val iw = imageSize.width.toFloat()
        val ih = imageSize.height.toFloat()
        if (vw > 0f && vh > 0f && iw > 0f && ih > 0f) {
            val fitScale = minOf(vw / iw, vh / ih)
            val displayedW = iw * fitScale
            val displayedH = ih * fitScale
            val offsetX = (vw - displayedW) / 2f
            val offsetY = (vh - displayedH) / 2f
            Rect(offsetX, offsetY, offsetX + displayedW, offsetY + displayedH)
        } else {
            Rect.Zero
        }
    }

    var cropRect by remember(contentBounds) {
        if (contentBounds != Rect.Zero) {
            val inset = 0.1f
            mutableStateOf(
                Rect(
                    left = contentBounds.left + contentBounds.width * inset,
                    top = contentBounds.top + contentBounds.height * inset,
                    right = contentBounds.right - contentBounds.width * inset,
                    bottom = contentBounds.bottom - contentBounds.height * inset
                )
            )
        } else {
            mutableStateOf(Rect.Zero)
        }
    }
    var activeHandle by remember { mutableStateOf(HandleType.NONE) }
    var dragStartPointer by remember { mutableStateOf(Offset.Zero) }
    var dragStartRect by remember { mutableStateOf(Rect.Zero) }

    LaunchedEffect(cropRect) {
        if (cropRect != Rect.Zero) {
            onCropRectChanged(cropRect)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            .testTag("imageCropOverlay")
            .pointerInput(contentBounds) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val hitHandle = hitTest(down.position, cropRect, handleRadiusPx)
                        if (hitHandle == HandleType.NONE) {
                            continue
                        }
                        activeHandle = hitHandle
                        dragStartPointer = down.position
                        dragStartRect = cropRect
                        down.consume()

                        var pointerId = down.id
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == pointerId }
                                ?: event.changes.firstOrNull()
                                ?: break

                            if (change.pressed) {
                                pointerId = change.id
                                cropRect = computeNewRect(
                                    handle = activeHandle,
                                    dragStartPointer = dragStartPointer,
                                    dragStartRect = dragStartRect,
                                    currentPointer = change.position,
                                    parentBounds = contentBounds,
                                    minSize = minSizePx
                                )
                                change.consume()
                            } else {
                                activeHandle = HandleType.NONE
                                break
                            }
                        }
                    }
                }
            }
    ) {
        if (cropRect == Rect.Zero) return@Canvas

        // Dim overlay outside crop
        drawRect(
            color = Color.Black.copy(alpha = 0.4f),
            topLeft = Offset.Zero,
            size = this.size
        )
        // Clear the crop area
        drawRect(
            color = Color.Transparent,
            topLeft = Offset(cropRect.left, cropRect.top),
            size = Size(cropRect.width, cropRect.height),
            blendMode = androidx.compose.ui.graphics.BlendMode.Clear
        )
        // Dashed border
        drawRect(
            color = Color.White,
            topLeft = Offset(cropRect.left, cropRect.top),
            size = Size(cropRect.width, cropRect.height),
            style = Stroke(
                width = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )
        )
        // Corner handles
        val corners = listOf(
            Offset(cropRect.left, cropRect.top),
            Offset(cropRect.right, cropRect.top),
            Offset(cropRect.left, cropRect.bottom),
            Offset(cropRect.right, cropRect.bottom)
        )
        for (corner in corners) {
            drawCircle(
                color = Color.White,
                radius = handleDrawRadius,
                center = corner
            )
            drawCircle(
                color = Color.DarkGray,
                radius = handleDrawRadius,
                center = corner,
                style = Stroke(width = 2f)
            )
        }
    }
}
