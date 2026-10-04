package com.novacut.editor.engine

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** Built-in motion-compensated smoothing. Optional neural processing is handled by [RifeEngine]. */
@Singleton
class FrameInterpolationEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ffmpegEngine: FFmpegEngine,
) {
    fun isAvailable(): Boolean = ffmpegEngine.isAvailable()

    suspend fun smoothMotion(
        inputUri: Uri, outputFile: File, sourceDurationMs: Long,
        onProgress: (Float) -> Unit = {},
    ): Boolean = withContext(Dispatchers.IO) {
        if (!ffmpegEngine.supportsMotionInterpolation()) return@withContext false
        val extractor = MediaExtractor()
        val sourceRate = try {
            extractor.setDataSource(context, inputUri, null)
            (0 until extractor.trackCount).asSequence().map(extractor::getTrackFormat)
                .firstOrNull { it.getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true }
                ?.let { format ->
                    runCatching { format.getInteger(MediaFormat.KEY_FRAME_RATE).toFloat() }.getOrNull()
                        ?: runCatching { format.getFloat(MediaFormat.KEY_FRAME_RATE) }.getOrNull()
                }?.takeIf { it.isFinite() && it > 0f } ?: 30f
        } finally {
            extractor.release()
        }
        // Never down-convert an already high-cadence source to satisfy the processing cap.
        if (sourceRate >= 120f) return@withContext false
        val targetFps = (sourceRate * 2f).toInt().coerceIn(60, 120)
        ffmpegEngine.interpolateMotion(inputUri, outputFile, sourceDurationMs, targetFps, onProgress)
    }
}
