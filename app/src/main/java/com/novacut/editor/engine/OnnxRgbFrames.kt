package com.novacut.editor.engine

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import java.nio.FloatBuffer
import kotlin.math.roundToInt

/** Float32 NCHW RGB in [0,1]. RIFE padding repeats the nearest edge, then output is cropped. */
object OnnxRgbFrames {
    /** Resample the original alpha independently of neural RGB, across the complete output. */
    internal fun preserveAlpha(output: Bitmap, source: Bitmap) {
        if (!source.hasAlpha()) return
        require(output.isMutable && output.hasAlpha())
        val maskPaint = Paint(Paint.FILTER_BITMAP_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        }
        // DST_IN uses only source alpha. Canvas handles premultiplication and avoids
        // a full-sized mask allocation or discontinuities at inference tile edges.
        Canvas(output).drawBitmap(source, null, Rect(0, 0, output.width, output.height), maskPaint)
    }

    fun tensor(environment: OrtEnvironment, bitmap: Bitmap, paddedWidth: Int = bitmap.width,
               paddedHeight: Int = bitmap.height): OnnxTensor {
        require(paddedWidth >= bitmap.width && paddedHeight >= bitmap.height)
        val pixels = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val plane = paddedWidth * paddedHeight
        val rgb = FloatArray(plane * 3)
        for (y in 0 until paddedHeight) for (x in 0 until paddedWidth) {
            val pixel = pixels[y.coerceAtMost(bitmap.height - 1) * bitmap.width + x.coerceAtMost(bitmap.width - 1)]
            val offset = y * paddedWidth + x
            rgb[offset] = ((pixel ushr 16) and 255) / 255f
            rgb[plane + offset] = ((pixel ushr 8) and 255) / 255f
            rgb[2 * plane + offset] = (pixel and 255) / 255f
        }
        return OnnxTensor.createTensor(environment, FloatBuffer.wrap(rgb), longArrayOf(1, 3, paddedHeight.toLong(), paddedWidth.toLong()))
    }

    fun bitmap(tensor: OnnxTensor, width: Int, height: Int): Bitmap {
        val shape = tensor.info.shape
        require(shape.size == 4 && shape[0] == 1L && shape[1] == 3L && shape[2] >= height && shape[3] >= width)
        val paddedWidth = shape[3].toInt()
        val plane = Math.multiplyExact(shape[2].toInt(), paddedWidth)
        val buffer = tensor.floatBuffer
        val pixels = IntArray(width * height)
        fun channel(offset: Int): Int {
            val value = buffer.get(offset)
            require(value.isFinite()) { "Non-finite neural output" }
            return (value.coerceIn(0f, 1f) * 255f).roundToInt()
        }
        for (y in 0 until height) for (x in 0 until width) {
            val i = y * paddedWidth + x
            pixels[y * width + x] = (255 shl 24) or (channel(i) shl 16) or (channel(plane + i) shl 8) or channel(2 * plane + i)
        }
        return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
}
