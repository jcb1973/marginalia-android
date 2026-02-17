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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
    CAMERA, CROP, PROCESSING
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
    var recognizedText by remember { mutableStateOf("") }

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
                    overlaySize = imageViewSize,
                    imageSize = IntSize(bitmap.width, bitmap.height),
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
                                runOcrText(croppedBitmap) { text ->
                                    if (text.isNotBlank()) {
                                        onTextRecognized(text)
                                    } else {
                                        recognizedText = ""
                                        phase = CapturePhase.CROP
                                    }
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

    val vw = viewSize.width.toFloat()
    val vh = viewSize.height.toFloat()
    val iw = original.width.toFloat()
    val ih = original.height.toFloat()

    // Match ContentScale.Fit: compute where the image is displayed in the view
    val fitScale = minOf(vw / iw, vh / ih)
    val displayedW = iw * fitScale
    val displayedH = ih * fitScale
    val offsetX = (vw - displayedW) / 2f
    val offsetY = (vh - displayedH) / 2f

    // Map view coordinates to bitmap coordinates
    val left = ((cropRect.left - offsetX) / fitScale).toInt().coerceIn(0, original.width - 1)
    val top = ((cropRect.top - offsetY) / fitScale).toInt().coerceIn(0, original.height - 1)
    val width = (cropRect.width / fitScale).toInt().coerceIn(1, original.width - left)
    val height = (cropRect.height / fitScale).toInt().coerceIn(1, original.height - top)

    return Bitmap.createBitmap(original, left, top, width, height)
}

private fun runOcrText(bitmap: Bitmap, onResult: (String) -> Unit) {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val image = InputImage.fromBitmap(bitmap, 0)
    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            // Join lines within each block with spaces (same paragraph),
            // separate blocks with newlines (actual paragraph breaks)
            val text = visionText.textBlocks.joinToString("\n") { block ->
                block.lines.joinToString(" ") { line -> line.text }
            }
            onResult(text)
        }
        .addOnFailureListener {
            onResult("")
        }
}
