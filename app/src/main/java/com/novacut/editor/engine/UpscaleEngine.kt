package com.novacut.editor.engine

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ensureActive
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

/** Real-ESRGAN's compact x4v3 model, with bounded tiles instead of whole-frame activations. */
@Singleton
class UpscaleEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    downloads: ModelDownloadManager,
    private val video: NeuralVideoProcessor,
) {
    companion object {
        val MODEL = DownloadableOnnxModel.Spec(
            "realesrgan.general.x4v3.onnx", "realesr-general-x4v3.onnx",
            "https://huggingface.co/jonathanst29/tinier-upscale-models/resolve/899dc1e4b22bbf1955c2f1739c085edc080cb366/realesr-general-x4v3.onnx",
            4_866_396L, "924ebad6532777303582d4ce7811849b88869231a3ae7093e0f21200249df8d5",
        )
    }
    private val model = DownloadableOnnxModel(context, downloads, MODEL)
    val modelState get() = model.state
    val downloadProgress get() = model.progress
    fun isModelReady() = model.isReady()
    suspend fun downloadModel(wifiOnly: Boolean = false) = model.download(wifiOnly)
    suspend fun deleteModel() = model.delete()

    suspend fun upscaleFrame(bitmap: Bitmap, onProgress: (Float) -> Unit = {}): Bitmap {
        NeuralMediaPolicy.requireDimensions(bitmap.width, bitmap.height, 2, NeuralMediaPolicy.MAX_IMAGE_OUTPUT_PIXELS)
        requireNeuralMemory(context, bitmap.width.toLong() * bitmap.height * 20 + 32L * 1024 * 1024, 192L * 1024 * 1024)
        return model.withSession { environment, session -> upscale(environment, session, bitmap, onProgress) }
    }

    suspend fun upscaleVideo(uri: Uri, output: File, onProgress: (Float) -> Unit = {}): Boolean {
        val source = video.source(uri)
        NeuralMediaPolicy.requireDimensions(source.width, source.height, 2, NeuralMediaPolicy.MAX_VIDEO_OUTPUT_PIXELS)
        requireNeuralMemory(context, source.width.toLong() * source.height * 20 + 32L * 1024 * 1024, 192L * 1024 * 1024)
        return model.withSession { environment, session ->
            video.render(uri, output, source, 1, 2, onProgress) { frame, _, _ ->
                upscale(environment, session, frame) {}
            }
        }
    }

    private suspend fun upscale(environment: OrtEnvironment, session: OrtSession, input: Bitmap,
                                progress: (Float) -> Unit): Bitmap {
        require(session.inputNames == setOf("input"))
        val output = Bitmap.createBitmap(input.width * 2, input.height * 2, Bitmap.Config.ARGB_8888)
        var complete = false
        try {
            val canvas = Canvas(output)
            val tiles = NeuralMediaPolicy.tiles(input.width, input.height)
            for ((index, tile) in tiles.withIndex()) {
                coroutineContext.ensureActive()
                val crop = Bitmap.createBitmap(input, tile.left, tile.top, tile.right - tile.left, tile.bottom - tile.top)
                var inferred: Bitmap? = null
                try {
                    OnnxRgbFrames.tensor(environment, crop).use { tensor ->
                        session.run(mapOf("input" to tensor)).use { result ->
                            coroutineContext.ensureActive()
                            inferred = OnnxRgbFrames.bitmap(result[0] as OnnxTensor, crop.width * 4, crop.height * 4)
                        }
                    }
                    // Model predicts 4x; downsample to the advertised 2x output without holding a full 4x bitmap.
                    val left = (tile.x - tile.left) * 4
                    val top = (tile.y - tile.top) * 4
                    canvas.drawBitmap(requireNotNull(inferred), Rect(left, top, left + tile.width * 4, top + tile.height * 4),
                        Rect(tile.x * 2, tile.y * 2, (tile.x + tile.width) * 2, (tile.y + tile.height) * 2),
                        android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG))
                } finally {
                    inferred?.recycle()
                    if (crop !== input) crop.recycle()
                }
                progress((index + 1f) / tiles.size)
            }
            complete = true
            return output
        } finally { if (!complete) output.recycle() }
    }
}
