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
    fun uriToCompressedBase64(context: Context?, uriString: String?, maxDimension: Int = 850, quality: Int = 85): String? {
        if (uriString.isNullOrBlank()) return null
        // If already a web URL or data URL, return as is
        if (uriString.startsWith("http://") || uriString.startsWith("https://") || uriString.startsWith("data:image")) {
            return uriString
        }
        if (context == null) return uriString

        return try {
            val uri = Uri.parse(uriString)

            // 1. Decode bounds only to prevent OutOfMemoryError on big camera images
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }
            val origWidth = boundsOptions.outWidth
            val origHeight = boundsOptions.outHeight
            if (origWidth <= 0 || origHeight <= 0) return uriString

            // 2. Compute inSampleSize to downsample while decoding
            var inSampleSize = 1
            var halfWidth = origWidth / 2
            var halfHeight = origHeight / 2
            while (halfWidth >= maxDimension && halfHeight >= maxDimension) {
                inSampleSize *= 2
                halfWidth /= 2
                halfHeight /= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            val sampledBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return uriString

            // 3. Scale down precisely to maxDimension
            val ratio = minOf(
                maxDimension.toFloat() / sampledBitmap.width,
                maxDimension.toFloat() / sampledBitmap.height,
                1.0f
            )
            val finalWidth = (sampledBitmap.width * ratio).toInt().coerceAtLeast(1)
            val finalHeight = (sampledBitmap.height * ratio).toInt().coerceAtLeast(1)
            val scaled = if (finalWidth != sampledBitmap.width || finalHeight != sampledBitmap.height) {
                val s = Bitmap.createScaledBitmap(sampledBitmap, finalWidth, finalHeight, true)
                sampledBitmap.recycle()
                s
            } else {
                sampledBitmap
            }

            val outputStream = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            scaled.recycle()
            val bytes = outputStream.toByteArray()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            "data:image/jpeg;base64,$base64"
        } catch (t: Throwable) {
            android.util.Log.e("ImageCompressor", "Failed to compress image", t)
            uriString
        }
    }

    private val base64Cache = android.util.LruCache<String, Bitmap>(60)

    /**
     * Decodes a Base64 data URL string ("data:image/jpeg;base64,...") into an Android Bitmap
     * so it can be rendered by Coil's AsyncImage or Compose Image without failing.
     */
    fun base64ToBitmap(dataUri: String?): Bitmap? {
        if (dataUri.isNullOrBlank() || !dataUri.startsWith("data:image")) return null
        val cached = base64Cache.get(dataUri)
        if (cached != null && !cached.isRecycled) return cached
        return try {
            val base64 = dataUri.substringAfter("base64,")
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bitmap != null) {
                base64Cache.put(dataUri, bitmap)
            }
            bitmap
        } catch (t: Throwable) {
            android.util.Log.e("ImageCompressor", "Failed to decode base64 bitmap", t)
            null
        }
    }

    /**
     * Resolves the model parameter for Coil's AsyncImage:
     * If uri is a Base64 data URI, returns the decoded Bitmap.
     * Otherwise returns the uri string (e.g. content://, file://, http://).
     */
    fun resolveImageModel(uriString: String?): Any? {
        if (uriString.isNullOrBlank()) return null
        if (uriString.startsWith("data:image")) {
            return base64ToBitmap(uriString) ?: uriString
        }
        return uriString
    }
}
