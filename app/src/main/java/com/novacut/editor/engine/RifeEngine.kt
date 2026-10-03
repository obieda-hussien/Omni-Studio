package com.novacut.editor.engine

import ai.onnxruntime.OnnxTensor
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ensureActive
import java.io.File
import java.nio.FloatBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

/** Practical-RIFE 4.9, ONNX export. No Vulkan/NCNN requirement or extra native binary. */
@Singleton
class RifeEngine @Inject constructor(
    @ApplicationContext context: Context,
    downloads: ModelDownloadManager,
    private val video: NeuralVideoProcessor,
) {
    companion object {
        val MODEL = DownloadableOnnxModel.Spec(
            "rife.v4.9.onnx", "rife49.onnx",
            "https://huggingface.co/yuvraj108c/rife-onnx/resolve/64de7265b6a06637c2f2c6a92ecd1972326499fe/rife49_ensemble_True_scale_1_sim.onnx",
            21_458_882L, "76e4cef9ab42fa7dd4e8f6e4aba47462051e3faa969e4bca6479784fbab0ac6f",
        )
    }
    private val model = DownloadableOnnxModel(context, downloads, MODEL)
    val modelState get() = model.state
    val downloadProgress get() = model.progress
    fun isModelReady() = model.isReady()
    suspend fun downloadModel(wifiOnly: Boolean = false) = model.download(wifiOnly)
    suspend fun deleteModel() = model.delete()

    suspend fun interpolateVideo(uri: Uri, output: File, progress: (Float) -> Unit): Boolean {
        val source = video.source(uri)
        NeuralMediaPolicy.requireDimensions(source.width, source.height, 1, NeuralMediaPolicy.MAX_RIFE_PIXELS)
        if (source.fps > 60) throw NeuralMediaException(NeuralMediaException.Reason.CADENCE)
        return model.withSession { environment, session ->
            require(session.inputNames == setOf("img0", "img1", "timestep"))
            video.render(uri, output, source, 2, 1, progress) { first, second, time ->
                coroutineContext.ensureActive()
                if (time == 0f || first === second || first.sameAs(second)) first else {
                    val width = (first.width + 31) / 32 * 32
                    val height = (first.height + 31) / 32 * 32
                    OnnxRgbFrames.tensor(environment, first, width, height).use { input0 ->
                        OnnxRgbFrames.tensor(environment, second, width, height).use { input1 ->
                            OnnxTensor.createTensor(environment, FloatBuffer.wrap(floatArrayOf(time)), longArrayOf(1)).use { timestep ->
                                session.run(mapOf("img0" to input0, "img1" to input1, "timestep" to timestep)).use { result ->
                                    coroutineContext.ensureActive()
                                    OnnxRgbFrames.bitmap(result[0] as OnnxTensor, first.width, first.height)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
