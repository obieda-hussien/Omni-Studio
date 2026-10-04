package com.novacut.editor.engine

import kotlin.math.ceil
import kotlin.math.min

enum class NeuralModelState { NOT_DOWNLOADED, DOWNLOADING, READY, ERROR }

class NeuralMediaException(val reason: Reason) : Exception(reason.name) {
    enum class Reason { DIMENSIONS, STORAGE, FRAME_COUNT, CADENCE, MODEL, HDR, MEMORY }
}

/** Reject oversized jobs before allocation; never silently shorten or resize the source. */
object NeuralMediaPolicy {
    const val MAX_RIFE_PIXELS = 1280L * 720
    const val MAX_IMAGE_OUTPUT_PIXELS = 4096L * 4096
    const val MAX_VIDEO_OUTPUT_PIXELS = 1920L * 1080
    const val MAX_FRAMES = 3600
    const val MAX_SCRATCH_BYTES = 1024L * 1024 * 1024
    const val STORAGE_RESERVE_BYTES = 128L * 1024 * 1024

    fun requireMemory(heapBytes: Long, nativeBytes: Long, availableHeap: Long, availableSystem: Long) {
        require(heapBytes > 0 && nativeBytes >= 0)
        if (heapBytes > availableHeap || heapBytes + nativeBytes > availableSystem) {
            throw NeuralMediaException(NeuralMediaException.Reason.MEMORY)
        }
    }

    /** Recover standard cadence from microsecond timestamps without rounding NTSC to integers. */
    fun sourceFrameRate(timestampsUs: List<Long>, hint: Double?): Double {
        val times = timestampsUs.filter { it >= 0 }.distinct().sorted()
        val rate = if (times.size >= 8 && times.last() > times.first())
            (times.size - 1) * 1_000_000.0 / (times.last() - times.first())
        else hint?.takeIf { it.isFinite() && it > 0 } ?: 30.0
        // Integer-microsecond timestamp quantization can put 60 fps just above 60.
        // This narrow tolerance still distinguishes 59.94 from 60 and rejects 60.01.
        val standards = listOf(24000.0 / 1001, 30000.0 / 1001, 60000.0 / 1001, 120000.0 / 1001,
            12.0, 15.0, 24.0, 25.0, 30.0, 48.0, 50.0, 60.0, 90.0, 100.0, 120.0)
        return standards.firstOrNull { kotlin.math.abs(it - rate) < 0.005 } ?: rate
    }

    fun frameCount(durationMs: Long, fps: Double): Int {
        if (durationMs <= 0 || !fps.isFinite() || fps <= 0 || fps > 120) {
            throw NeuralMediaException(NeuralMediaException.Reason.CADENCE)
        }
        val count = ceil(durationMs / 1000.0 * fps)
        if (count > MAX_FRAMES) throw NeuralMediaException(NeuralMediaException.Reason.FRAME_COUNT)
        return count.toInt().coerceAtLeast(1)
    }

    fun requireDimensions(width: Int, height: Int, scale: Int, maximumPixels: Long) {
        if (width <= 0 || height <= 0 || scale !in 1..4 ||
            width.toLong() * height * scale * scale > maximumPixels ||
            width.toLong() * scale > 4096 || height.toLong() * scale > 4096) {
            throw NeuralMediaException(NeuralMediaException.Reason.DIMENSIONS)
        }
    }

    fun requireStorage(width: Int, height: Int, frames: Int, freeBytes: Long): Long {
        require(width > 0 && height > 0 && frames in 1..MAX_FRAMES)
        // PNG worst case + headers. Account for the final MP4 living beside the frames.
        val scratch = (width.toLong() * height * 4 + 65536L) * frames
        if (scratch > MAX_SCRATCH_BYTES || scratch * 2 + STORAGE_RESERVE_BYTES > freeBytes) {
            throw NeuralMediaException(NeuralMediaException.Reason.STORAGE)
        }
        return scratch
    }

    data class Tile(val x: Int, val y: Int, val width: Int, val height: Int,
                    val left: Int, val top: Int, val right: Int, val bottom: Int)

    /** Disjoint output cores with overlapping model context; every output pixel is written once. */
    fun tiles(width: Int, height: Int, core: Int = 128, padding: Int = 40): List<Tile> {
        require(width > 0 && height > 0 && core > 0 && padding >= 0)
        return buildList {
            for (y in 0 until height step core) for (x in 0 until width step core) {
                add(Tile(x, y, min(core, width - x), min(core, height - y),
                    (x - padding).coerceAtLeast(0), (y - padding).coerceAtLeast(0),
                    min(width, x + core + padding), min(height, y + core + padding)))
            }
        }
    }
}
