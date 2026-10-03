package com.novacut.editor.engine

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class NeuralVideoProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val ffmpeg: FFmpegEngine,
) {
    data class Source(val durationMs: Long, val fps: Double, val width: Int, val height: Int)

    suspend fun source(uri: Uri): Source = withContext(Dispatchers.IO) {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(context, uri, null)
            val track = (0 until extractor.trackCount).first {
                extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("video/") == true
            }
            val format = extractor.getTrackFormat(track)
            val transfer = runCatching { format.getInteger(MediaFormat.KEY_COLOR_TRANSFER) }.getOrDefault(0)
            if (transfer == MediaFormat.COLOR_TRANSFER_ST2084 || transfer == MediaFormat.COLOR_TRANSFER_HLG) {
                throw NeuralMediaException(NeuralMediaException.Reason.HDR)
            }
            val hint = (runCatching { format.getInteger(MediaFormat.KEY_FRAME_RATE).toDouble() }.getOrNull()
                ?: runCatching { format.getFloat(MediaFormat.KEY_FRAME_RATE).toDouble() }.getOrNull()
                ?: 30.0).takeIf { it.isFinite() && it > 0 } ?: 30.0
            extractor.selectTrack(track)
            val timestamps = buildList {
                repeat(96) {
                    val time = extractor.sampleTime
                    if (time >= 0) { add(time); extractor.advance() }
                }
            }
            val fps = NeuralMediaPolicy.sourceFrameRate(timestamps, hint)
            val rotation = runCatching { format.getInteger(MediaFormat.KEY_ROTATION) }.getOrDefault(0)
            val width = format.getInteger(MediaFormat.KEY_WIDTH)
            val height = format.getInteger(MediaFormat.KEY_HEIGHT)
            Source(format.getLong(MediaFormat.KEY_DURATION) / 1000, fps,
                if (rotation % 180 != 0) height else width, if (rotation % 180 != 0) width else height)
        } finally { extractor.release() }
    }

    /** Only two decoded inputs and one result are resident. Disk usage is preflighted and bounded. */
    suspend fun render(
        uri: Uri, output: File, source: Source, multiplier: Int, scale: Int,
        progress: (Float) -> Unit,
        transform: suspend (Bitmap, Bitmap, Float) -> Bitmap,
    ): Boolean = withContext(Dispatchers.IO) {
        val violation = NativeProcessingPolicy.validateVideoUri(context, uri, "neuralVideo")
        if (violation != null) return@withContext NativeProcessingPolicy.logAndReject(violation)
        require(multiplier in 1..2 && scale in 1..4)
        val fps = source.fps * multiplier
        val frames = NeuralMediaPolicy.frameCount(source.durationMs, fps)
        NeuralMediaPolicy.requireStorage(source.width * scale, source.height * scale, frames,
            min(context.cacheDir.usableSpace, output.parentFile?.usableSpace ?: context.cacheDir.usableSpace))
        val lease = CodecInstanceBudget.acquireRetriever(context.contentResolver.getType(uri))
        val retriever = lease.resource
        var temp: File? = null
        var first: Bitmap? = null
        var second: Bitmap? = null
        var retained = false
        try {
            val frameDir = File.createTempFile("neural-frames-", "", context.cacheDir)
            temp = frameDir
            check(frameDir.delete()); check(frameDir.mkdir())
            retriever.setDataSource(context, uri)
            fun decode(index: Int): Bitmap = retriever.getFrameAtTime(
                (index * 1_000_000.0 / source.fps).toLong().coerceAtMost(source.durationMs * 1000 - 1),
                MediaMetadataRetriever.OPTION_CLOSEST,
            ) ?: error("Could not decode frame")
            first = decode(0)
            second = if (multiplier == 2) decode(1) else null
            var lastInputIndex = 0
            var bytes = 0L
            for (index in 0 until frames) {
                ensureActive()
                val inputIndex = index / multiplier
                if (inputIndex != lastInputIndex) {
                    first?.recycle()
                    first = if (multiplier == 2) second else decode(inputIndex)
                    second = null
                    if (multiplier == 2) second = decode(inputIndex + 1)
                    lastInputIndex = inputIndex
                }
                val input = requireNotNull(first)
                val rendered = transform(input, second ?: input, (index % multiplier).toFloat() / multiplier)
                try {
                    val frame = File(frameDir, String.format(Locale.US, "frame_%05d.png", index))
                    frame.outputStream().use { check(rendered.compress(Bitmap.CompressFormat.PNG, 100, it)) }
                    bytes += frame.length()
                    if (bytes > NeuralMediaPolicy.MAX_SCRATCH_BYTES) throw NeuralMediaException(NeuralMediaException.Reason.STORAGE)
                } finally {
                    if (rendered !== first && rendered !== second) rendered.recycle()
                }
                progress((index + 1f) / frames * 0.9f)
            }
            ensureActive()
            val ok = ffmpeg.encodeImageSequenceWithAudio(
                uri, File(frameDir, "frame_%05d.png").absolutePath, fps, output,
                source.durationMs, onProgress = { progress(0.9f + it * 0.1f) },
            )
            ensureActive()
            retained = ok
            if (ok) progress(1f)
            ok
        } finally {
            first?.recycle(); second?.recycle(); lease.close()
            temp?.deleteRecursively()
            if (!retained) output.delete()
        }
    }
}
