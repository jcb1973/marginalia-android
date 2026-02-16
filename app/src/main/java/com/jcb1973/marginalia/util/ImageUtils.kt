package com.jcb1973.marginalia.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

object ImageUtils {

    private const val MAX_WIDTH = 600
    private const val JPEG_QUALITY = 70

    suspend fun downloadAndCompress(
        context: Context,
        imageUrl: String,
        fileName: String
    ): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val inputStream = url.openStream()
            val original = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (original == null) return@withContext null

            val scaled = scaleDown(original)
            val dir = File(context.filesDir, "covers")
            if (!dir.exists()) dir.mkdirs()

            val file = File(dir, "$fileName.jpg")
            FileOutputStream(file).use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }
            if (scaled !== original) scaled.recycle()
            original.recycle()

            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    fun saveBitmap(context: Context, bitmap: Bitmap, fileName: String): String? {
        return try {
            val dir = File(context.filesDir, "covers")
            if (!dir.exists()) dir.mkdirs()

            val scaled = scaleDown(bitmap)
            val file = File(dir, "$fileName.jpg")
            FileOutputStream(file).use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }
            if (scaled !== bitmap) scaled.recycle()

            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    private fun scaleDown(original: Bitmap): Bitmap {
        if (original.width <= MAX_WIDTH) return original
        val ratio = MAX_WIDTH.toFloat() / original.width
        val newHeight = (original.height * ratio).toInt()
        return Bitmap.createScaledBitmap(original, MAX_WIDTH, newHeight, true)
    }
}
