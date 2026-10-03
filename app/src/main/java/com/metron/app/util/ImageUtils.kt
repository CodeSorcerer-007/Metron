package com.metron.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.File

/**
 * Memory-safe bitmap decoding utility.
 * Guarantees that previewing receipt images never triggers OutOfMemoryError or UI freezes.
 */
object ImageUtils {

    private const val TAG = "ImageUtils"

    /**
     * Decodes a downsampled thumbnail bitmap suitable for UI previews (e.g. 120dp thumbnails).
     * Automatically calculates inSampleSize to prevent loading giant full-resolution images into memory.
     */
    fun loadThumbnail(path: String?, maxDimension: Int = 300): Bitmap? {
        if (path.isNullOrBlank()) return null
        val file = File(path)
        if (!file.exists() || !file.canRead()) return null

        return try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, boundsOptions)

            val rawW = boundsOptions.outWidth
            val rawH = boundsOptions.outHeight
            if (rawW <= 0 || rawH <= 0) return null

            var inSampleSize = 1
            if (rawW > maxDimension || rawH > maxDimension) {
                val halfW = rawW / 2
                val halfH = rawH / 2
                while ((halfW / inSampleSize) >= maxDimension && (halfH / inSampleSize) >= maxDimension) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.RGB_565 // Conserves 50% RAM compared to ARGB_8888
            }

            BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
        } catch (oom: OutOfMemoryError) {
            Log.e(TAG, "Out of memory decoding receipt thumbnail for: $path", oom)
            System.gc()
            null
        } catch (e: Throwable) {
            Log.e(TAG, "Error decoding receipt thumbnail for: $path", e)
            null
        }
    }
}
