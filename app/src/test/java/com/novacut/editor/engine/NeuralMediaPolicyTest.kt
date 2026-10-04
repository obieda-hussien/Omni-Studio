package com.novacut.editor.engine

import org.junit.Assert.*
import org.junit.Test

class NeuralMediaPolicyTest {
    @Test fun microsecondTimestampsSnapIntegerCadenceAtRifeLimit() {
        for (fps in listOf(24.0, 25.0, 30.0, 50.0, 60.0, 120.0)) {
            val timestamps = (0 until 96).map { (it * 1_000_000.0 / fps).toLong() }
            val actual = NeuralMediaPolicy.sourceFrameRate(timestamps, fps)
            assertEquals(fps, actual, 1e-9)
            val multiplier = if (fps <= 60) 2 else 1
            assertEquals((fps * multiplier).toInt(), NeuralMediaPolicy.frameCount(1000, actual * multiplier))
        }
    }
    @Test fun normalizationKeepsNtscAndGenuinelyHigherCadenceDistinct() {
        for (fps in listOf(24000.0 / 1001, 60000.0 / 1001)) {
            val timestamps = (0 until 96).map { (it * 1_000_000.0 / fps).toLong() }
            assertEquals(fps, NeuralMediaPolicy.sourceFrameRate(timestamps, fps), 1e-9)
        }
        val tooFast = (0 until 96).map { (it * 1_000_000.0 / 60.01).toLong() }
        assertTrue(NeuralMediaPolicy.sourceFrameRate(tooFast, 60.0) > 60.005)
    }
    @Test fun memoryChecksBothHeapAndNativeHeadroomBeforeAllocation() {
        NeuralMediaPolicy.requireMemory(100, 200, 100, 300)
        assertReason(NeuralMediaException.Reason.MEMORY) { NeuralMediaPolicy.requireMemory(100, 200, 99, 300) }
        assertReason(NeuralMediaException.Reason.MEMORY) { NeuralMediaPolicy.requireMemory(100, 200, 100, 299) }
    }
    @Test fun encodedTimestampsRecoverFractionalCadenceAndTolerateReorderedFrames() {
        val timestamps = (0 until 96).map { (it * 1_000_000.0 / (30000.0 / 1001)).toLong() }
        assertEquals(30000.0 / 1001, NeuralMediaPolicy.sourceFrameRate(timestamps.reversed(), 30.0), 1e-9)
        assertEquals(24.0, NeuralMediaPolicy.sourceFrameRate(emptyList(), 24.0), 1e-9)
        assertEquals(30.0, NeuralMediaPolicy.sourceFrameRate(emptyList(), Double.NaN), 1e-9)
    }
    @Test fun fractionalCadenceKeepsTheLastPartialFrame() {
        assertEquals(60, NeuralMediaPolicy.frameCount(1001, 60000.0 / 1001))
        assertEquals(49, NeuralMediaPolicy.frameCount(1001, 48.0))
    }
    @Test fun longSourcesAreRejectedInsteadOfTruncated() {
        assertReason(NeuralMediaException.Reason.FRAME_COUNT) { NeuralMediaPolicy.frameCount(60_001, 60.0) }
        assertEquals(3600, NeuralMediaPolicy.frameCount(60_000, 60.0))
    }
    @Test fun invalidAndTooFastCadenceAreRejected() {
        for (fps in listOf(Double.NaN, Double.POSITIVE_INFINITY, 0.0, -1.0, 120.01)) {
            assertReason(NeuralMediaException.Reason.CADENCE) { NeuralMediaPolicy.frameCount(1000, fps) }
        }
    }
    @Test fun outputBudgetIncludesTheUpscaleFactor() {
        NeuralMediaPolicy.requireDimensions(960, 540, 2, NeuralMediaPolicy.MAX_VIDEO_OUTPUT_PIXELS)
        assertReason(NeuralMediaException.Reason.DIMENSIONS) {
            NeuralMediaPolicy.requireDimensions(1920, 1080, 2, NeuralMediaPolicy.MAX_VIDEO_OUTPUT_PIXELS)
        }
        NeuralMediaPolicy.requireDimensions(720, 1280, 1, NeuralMediaPolicy.MAX_RIFE_PIXELS)
    }
    @Test fun storageReservesBothFramesAndFinalOutput() {
        val frameBytes = (320L * 180 * 4 + 65536) * 60
        val required = frameBytes * 2 + NeuralMediaPolicy.STORAGE_RESERVE_BYTES
        assertEquals(frameBytes, NeuralMediaPolicy.requireStorage(320, 180, 60, required))
        assertReason(NeuralMediaException.Reason.STORAGE) { NeuralMediaPolicy.requireStorage(320, 180, 60, required - 1) }
    }
    @Test fun scratchCapIsEnforcedEvenOnLargeDisks() {
        assertReason(NeuralMediaException.Reason.STORAGE) { NeuralMediaPolicy.requireStorage(1280, 720, 1000, Long.MAX_VALUE) }
    }
    @Test fun partialTilesCoverPortraitLandscapeAndTinyImagesExactlyOnce() {
        for ((width, height) in listOf(513 to 257, 257 to 513, 1 to 1, 128 to 128)) {
            val coverage = IntArray(width * height)
            NeuralMediaPolicy.tiles(width, height).forEach { t ->
                assertTrue(t.left <= t.x && t.top <= t.y)
                assertTrue(t.right >= t.x + t.width && t.bottom >= t.y + t.height)
                assertTrue(t.left >= 0 && t.top >= 0 && t.right <= width && t.bottom <= height)
                for (y in t.y until t.y + t.height) for (x in t.x until t.x + t.width) coverage[y * width + x]++
            }
            assertTrue(coverage.all { it == 1 })
        }
    }
    private fun assertReason(reason: NeuralMediaException.Reason, operation: () -> Unit) {
        try { operation(); fail("Expected $reason") }
        catch (error: NeuralMediaException) { assertEquals(reason, error.reason) }
    }
}
