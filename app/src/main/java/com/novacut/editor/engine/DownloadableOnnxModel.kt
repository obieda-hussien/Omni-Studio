package com.novacut.editor.engine

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/** A model's downloads, deletion and inference share one owner; no removal during a running job. */
class DownloadableOnnxModel(
    context: Context,
    private val downloads: ModelDownloadManager,
    val spec: Spec,
) {
    data class Spec(val id: String, val filename: String, val url: String,
                    val bytes: Long, val sha256: String)
    private val file = File(context.filesDir, "models/${spec.id}/${spec.filename}")
    private val mutex = Mutex()
    private val _state = MutableStateFlow(if (isReady()) NeuralModelState.READY else NeuralModelState.NOT_DOWNLOADED)
    val state = _state.asStateFlow()
    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()
    fun isReady(): Boolean = file.isFile && file.length() == spec.bytes

    suspend fun download(wifiOnly: Boolean): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            _state.value = NeuralModelState.DOWNLOADING
            _progress.value = 0f
            try {
                downloads.downloadFiles(listOf(ModelDownloadManager.ModelFile(
                    url = spec.url, targetFile = file, minimumBytes = spec.bytes,
                    estimatedBytes = spec.bytes, maxBytes = spec.bytes,
                    sha256 = spec.sha256, checksumRequired = true,
                )), wifiOnly = wifiOnly, onProgress = { _progress.value = it })
                _state.value = NeuralModelState.READY
                true
            } catch (cancelled: CancellationException) {
                _state.value = if (isReady()) NeuralModelState.READY else NeuralModelState.NOT_DOWNLOADED
                throw cancelled
            } catch (error: Exception) {
                _state.value = NeuralModelState.ERROR
                throw error
            }
        }
    }

    suspend fun delete() = withContext(Dispatchers.IO) {
        mutex.withLock {
            if (file.exists() && !downloads.removeModel(file)) error("Could not remove model")
            _state.value = NeuralModelState.NOT_DOWNLOADED
            _progress.value = 0f
        }
    }

    suspend fun <T> withSession(block: suspend (OrtEnvironment, OrtSession) -> T): T =
        withContext(Dispatchers.Default) {
            mutex.withLock {
                if (!ModelDownloadManager.verifyChecksumOrDelete(file, spec.bytes, spec.sha256)) {
                    _state.value = NeuralModelState.NOT_DOWNLOADED
                    throw NeuralMediaException(NeuralMediaException.Reason.MODEL)
                }
                // OrtEnvironment is a process singleton, owned by the runtime, not by this job.
                val environment = OrtEnvironment.getEnvironment()
                OnnxSessionFactory.createSession(environment, file.absolutePath).use {
                    block(environment, it.session)
                }
            }
        }
}
