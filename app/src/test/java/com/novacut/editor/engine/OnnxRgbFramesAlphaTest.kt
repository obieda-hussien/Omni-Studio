package com.novacut.editor.engine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import java.io.ByteArrayOutputStream
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], application = android.app.Application::class)
class OnnxRgbFramesAlphaTest {
    @Test fun transparentAndOpaqueEdgesSurviveFilteringAndPngEncoding() {
        val source = Bitmap.createBitmap(2, 1, Bitmap.Config.ARGB_8888)
        val output = Bitmap.createBitmap(4, 2, Bitmap.Config.ARGB_8888)
        try {
            source.setPixel(0, 0, Color.TRANSPARENT)
            source.setPixel(1, 0, Color.RED)
            output.eraseColor(Color.BLUE)
            OnnxRgbFrames.preserveAlpha(output, source)
            for (y in 0..1) {
                assertEquals(0, Color.alpha(output.getPixel(0, y)))
                assertTrue(Color.alpha(output.getPixel(1, y)) in 63..65)
                assertTrue(Color.alpha(output.getPixel(2, y)) in 190..192)
                assertEquals(255, Color.alpha(output.getPixel(3, y)))
                assertEquals(Color.BLUE, output.getPixel(3, y))
            }
            val bytes = ByteArrayOutputStream().use {
                assertTrue(output.compress(Bitmap.CompressFormat.PNG, 100, it))
                it.toByteArray()
            }
            val decoded = requireNotNull(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            try {
                assertEquals(0, Color.alpha(decoded.getPixel(0, 0)))
                assertEquals(Color.alpha(output.getPixel(1, 0)), Color.alpha(decoded.getPixel(1, 0)))
                assertEquals(Color.BLUE, decoded.getPixel(3, 0))
            } finally { decoded.recycle() }
        } finally { output.recycle(); source.recycle() }
    }

    @Test fun semitransparentSourceRetainsNeuralRgbInsteadOfMaskColor() {
        val source = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val output = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        try {
            source.eraseColor(Color.argb(128, 255, 0, 0))
            output.eraseColor(Color.BLUE)
            OnnxRgbFrames.preserveAlpha(output, source)
            for (y in 0..1) for (x in 0..1) {
                assertEquals(Color.argb(128, 0, 0, 255), output.getPixel(x, y))
            }
        } finally { output.recycle(); source.recycle() }
    }

    @Test fun fullyTransparentSourceRemainsTransparentAcrossTileBoundaries() {
        val source = Bitmap.createBitmap(129, 1, Bitmap.Config.ARGB_8888)
        val output = Bitmap.createBitmap(258, 2, Bitmap.Config.ARGB_8888)
        try {
            output.eraseColor(Color.BLUE)
            OnnxRgbFrames.preserveAlpha(output, source)
            for (y in 0..1) for (x in 0 until 258) {
                assertEquals(0, Color.alpha(output.getPixel(x, y)))
            }
        } finally { output.recycle(); source.recycle() }
    }

    @Test fun opaqueInputLeavesEnhancedRgbUnchanged() {
        val source = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565)
        val output = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        try {
            source.eraseColor(Color.RED)
            output.eraseColor(Color.BLUE)
            OnnxRgbFrames.preserveAlpha(output, source)
            for (y in 0..1) for (x in 0..1) assertEquals(Color.BLUE, output.getPixel(x, y))
        } finally { output.recycle(); source.recycle() }
    }
}
