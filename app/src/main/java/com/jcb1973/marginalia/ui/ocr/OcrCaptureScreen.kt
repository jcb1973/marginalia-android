package com.jcb1973.marginalia.ui.ocr

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.jcb1973.marginalia.ui.components.ImageCropOverlay
import java.nio.ByteBuffer

private enum class CapturePhase {
    CAMERA, CROP, PROCESSING, SELECT
}

@Composable
fun OcrCaptureScreen(
    onTextRecognized: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var phase by remember { mutableStateOf(CapturePhase.CAMERA) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cropRect by remember { mutableStateOf<Rect?>(null) }
    var hasCameraPermission by remember { mutableStateOf(false) }
    var imageViewSize by remember { mutableStateOf(IntSize.Zero) }
    var recognizedLines by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedIndices by remember { mutableStateOf<Set<Int>>(emptySet()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    when (phase) {
        CapturePhase.CAMERA -> {
            if (!hasCameraPermission) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Camera permission required", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                            Text("Grant Permission")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = onCancel) { Text("Cancel") }
                    }
                }
                return
            }

            val imageCapture = remember { ImageCapture.Builder().build() }
            val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture
                            )
                        }, ContextCompat.getMainExecutor(ctx))
                        previewView
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("ocrCameraPreview")
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            imageCapture.takePicture(
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageCapturedCallback() {
                                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                        val bitmap = imageProxyToBitmap(imageProxy)
                                        imageProxy.close()
                                        if (bitmap != null) {
                                            capturedBitmap = bitmap
                                            phase = CapturePhase.CROP
                                        }
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        // Stay on camera phase
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("capturePhotoButton")
                    ) {
                        Text("Capture")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cancelOcrButton")
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }

        CapturePhase.CROP -> {
            val bitmap = capturedBitmap ?: run { phase = CapturePhase.CAMERA; return }

            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Captured page",
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { imageViewSize = it },
                    contentScale = ContentScale.Fit
                )

                ImageCropOverlay(
                    onCropRectChanged = { rect -> cropRect = rect }
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row {
                        OutlinedButton(
                            onClick = {
                                capturedBitmap = null
                                cropRect = null
                                phase = CapturePhase.CAMERA
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("retakeButton")
                        ) {
                            Text("Retake")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                phase = CapturePhase.PROCESSING
                                val croppedBitmap = cropBitmap(bitmap, cropRect, imageViewSize)
                                runOcrLines(croppedBitmap) { lines ->
                                    recognizedLines = lines
                                    selectedIndices = emptySet()
                                    phase = CapturePhase.SELECT
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("extractTextButton")
                        ) {
                            Text(if (cropRect != null) "Extract Text" else "Extract All")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }

        CapturePhase.PROCESSING -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.testTag("ocrProgress"))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Recognizing text...", style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        CapturePhase.SELECT -> {
            if (recognizedLines.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No text found",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Try photographing the page again",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                capturedBitmap = null
                                cropRect = null
                                phase = CapturePhase.CAMERA
                            },
                            modifier = Modifier.testTag("retakeFromSelectButton")
                        ) {
                            Text("Retake")
                        }
                    }
                }
            } else {
                LineSelectionContent(
                    lines = recognizedLines,
                    selectedIndices = selectedIndices,
                    onToggleLine = { index ->
                        selectedIndices = if (index in selectedIndices) {
                            selectedIndices - index
                        } else {
                            selectedIndices + index
                        }
                    },
                    onSelectAll = {
                        selectedIndices = recognizedLines.indices.toSet()
                    },
                    onClearAll = {
                        selectedIndices = emptySet()
                    },
                    onRetake = {
                        capturedBitmap = null
                        cropRect = null
                        phase = CapturePhase.CAMERA
                    },
                    onUseSelected = {
                        val selectedText = selectedIndices.sorted()
                            .map { recognizedLines[it] }
                            .joinToString("\n")
                        onTextRecognized(selectedText)
                    }
                )
            }
        }
    }
}

@Composable
private fun LineSelectionContent(
    lines: List<String>,
    selectedIndices: Set<Int>,
    onToggleLine: (Int) -> Unit,
    onSelectAll: () -> Unit,
    onClearAll: () -> Unit,
    onRetake: () -> Unit,
    onUseSelected: () -> Unit
) {
    val allSelected = selectedIndices.size == lines.size

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(tonalElevation = 2.dp) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Select lines",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedIndices.size} of ${lines.size} selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = if (allSelected) onClearAll else onSelectAll,
                        modifier = Modifier.testTag("selectAllToggle")
                    ) {
                        Text(if (allSelected) "Clear" else "Select All")
                    }
                }
            }
        }

        HorizontalDivider()

        // Line list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag("ocrLinesList")
        ) {
            itemsIndexed(lines) { index, line ->
                val isSelected = index in selectedIndices
                Surface(
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleLine(index) }
                        .testTag("ocrLine_$index")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                if (index < lines.lastIndex) {
                    HorizontalDivider()
                }
            }
        }

        // Bottom buttons
        HorizontalDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onRetake,
                modifier = Modifier
                    .weight(1f)
                    .testTag("retakeFromSelectButton")
            ) {
                Text("Retake")
            }
            Button(
                onClick = onUseSelected,
                enabled = selectedIndices.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .testTag("useSelectedButton")
            ) {
                Text("Use Selected")
            }
        }
    }
}

private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    val buffer: ByteBuffer = imageProxy.planes[0].buffer
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null

    val rotation = imageProxy.imageInfo.rotationDegrees
    return if (rotation != 0) {
        val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }
}

private fun cropBitmap(original: Bitmap, cropRect: Rect?, viewSize: IntSize): Bitmap {
    if (cropRect == null || viewSize.width == 0 || viewSize.height == 0) return original

    val scaleX = original.width.toFloat() / viewSize.width
    val scaleY = original.height.toFloat() / viewSize.height

    val left = (cropRect.left * scaleX).toInt().coerceIn(0, original.width - 1)
    val top = (cropRect.top * scaleY).toInt().coerceIn(0, original.height - 1)
    val width = (cropRect.width * scaleX).toInt().coerceIn(1, original.width - left)
    val height = (cropRect.height * scaleY).toInt().coerceIn(1, original.height - top)

    return Bitmap.createBitmap(original, left, top, width, height)
}

private fun runOcrLines(bitmap: Bitmap, onResult: (List<String>) -> Unit) {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val image = InputImage.fromBitmap(bitmap, 0)
    recognizer.process(image)
        .addOnSuccessListener { text ->
            val lines = text.textBlocks
                .flatMap { block -> block.lines }
                .map { line -> line.text }
            onResult(lines)
        }
        .addOnFailureListener {
            onResult(emptyList())
        }
}
