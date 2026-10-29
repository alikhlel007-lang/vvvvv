package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

object ImageCompressor {
    /**
     * Converts a local content:// or file:// URI into a compact JPEG Base64 Data URL.
     * Web browsers (like the Customer Menu in index.html) and Android Coil can render
     * this data URL immediately without needing external storage uploads.
     */
    fun uriToCompressedBase64(context: Context?, uriString: String?, maxDimension: Int = 450, quality: Int = 75): String? {
        if (uriString.isNullOrBlank()) return null
        // If already a web URL or data URL, return as is
        if (uriString.startsWith("http://") || uriString.startsWith("https://") || uriString.startsWith("data:image")) {
            return uriString
        }
        if (context == null) return uriString

        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri) ?: return uriString
            val original = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (original == null) return uriString

            val ratio = minOf(
                maxDimension.toFloat() / original.width,
                maxDimension.toFloat() / original.height,
                1.0f
            )
            val newWidth = (original.width * ratio).toInt().coerceAtLeast(1)
            val newHeight = (original.height * ratio).toInt().coerceAtLeast(1)
            val scaled = Bitmap.createScaledBitmap(original, newWidth, newHeight, true)

            val outputStream = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val bytes = outputStream.toByteArray()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64"
        } catch (_: Exception) {
            uriString
        }
    }
}
