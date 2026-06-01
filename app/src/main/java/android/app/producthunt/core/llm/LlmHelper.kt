package android.app.producthunt.core.llm

import android.app.producthunt.core.log.ILog
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

class LlmHelper(
    private val context: Context,
) {
    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    fun getModelFile(model: LlmModelSpec): File {
        val modelDir = File(modelsDir, model.cacheKey())
        val fileName = model.filename.substringAfterLast('/')
        return File(modelDir, fileName)
    }

    fun isModelDownloaded(model: LlmModelSpec): Boolean {
        val file = getModelFile(model)
        val isDownloaded = file.exists() && file.length() > 0L
        ILog.d(TAG, "isModelDownloaded", model.filename, isDownloaded, file.absolutePath, file.length())
        return isDownloaded
    }

    suspend fun getModelPath(model: LlmModelSpec): String? =
        getModelFile(model).takeIf { isModelDownloaded(model) }?.absolutePath

    fun downloadModel(
        model: LlmModelSpec,
        force: Boolean = false,
    ): Flow<LlmModelDownloadEvent> = flow {
        val targetFile = getModelFile(model)
        val partialFile = File(targetFile.parentFile, "${targetFile.name}.part")
        val url = model.huggingFaceResolveUrl()

        ILog.i(TAG, "downloadModel", "starting", model.repoId, model.filename)
        emit(LlmModelDownloadEvent.Starting(url))

        try {
            targetFile.parentFile?.mkdirs()

            if (force) {
                targetFile.delete()
                partialFile.delete()
            }

            if (isModelDownloaded(model)) {
                ILog.i(TAG, "downloadModel", "already downloaded", targetFile.absolutePath)
                emit(LlmModelDownloadEvent.Completed(targetFile.absolutePath))
                return@flow
            }

            val existingBytes = partialFile.takeIf { it.exists() }?.length() ?: 0L
            val requestBuilder = Request.Builder().url(url)
            if (existingBytes > 0L) {
                requestBuilder.header("Range", "bytes=$existingBytes-")
            }

            client.newCall(requestBuilder.build()).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("Download failed: HTTP ${response.code}")
                }

                val canResume = response.code == 206 && existingBytes > 0L
                val alreadyDownloaded = if (canResume) existingBytes else 0L
                val body = response.body
                val contentLength = body.contentLength().takeIf { it >= 0L }
                val totalBytes = contentLength?.plus(alreadyDownloaded)

                if (!canResume) {
                    partialFile.delete()
                }

                body.byteStream().use { input ->
                    FileOutputStream(partialFile, canResume).use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var downloadedBytes = alreadyDownloaded

                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break

                            output.write(buffer, 0, read)
                            downloadedBytes += read

                            emit(
                                LlmModelDownloadEvent.Progress(
                                    downloadedBytes = downloadedBytes,
                                    totalBytes = totalBytes,
                                    percent = totalBytes?.let { downloadedBytes.toFloat() / it.toFloat() },
                                )
                            )
                        }
                    }
                }
            }

            verifySha256IfNeeded(model, partialFile)

            if (!partialFile.renameTo(targetFile)) {
                partialFile.copyTo(targetFile, overwrite = true)
                partialFile.delete()
            }

            emit(LlmModelDownloadEvent.Completed(targetFile.absolutePath))
        } catch (e: Exception) {
            ILog.e(TAG, "downloadModel", "failed", model.filename, throwable = e)
            emit(LlmModelDownloadEvent.Failed(e.message ?: "Model download failed", e))
        }
    }.flowOn(Dispatchers.IO)

    suspend fun deleteModel(model: LlmModelSpec): Boolean {
        val targetFile = getModelFile(model)
        val partialFile = File(targetFile.parentFile, "${targetFile.name}.part")
        val modelDir = targetFile.parentFile

        val targetDeleted = !targetFile.exists() || targetFile.delete()
        val partialDeleted = !partialFile.exists() || partialFile.delete()
        modelDir?.delete()

        ILog.i(TAG, "deleteModel", model.filename, targetDeleted && partialDeleted)
        return targetDeleted && partialDeleted
    }

    suspend fun deleteAllModels(): Boolean {
        val deleted = !modelsDir.exists() || modelsDir.deleteRecursively()
        ILog.i(TAG, "deleteAllModels", deleted)
        return deleted
    }

    private val modelsDir: File
        get() = File(context.filesDir, "litertlm/models")

    private fun LlmModelSpec.huggingFaceResolveUrl(): String =
        "https://huggingface.co/$repoId/resolve/$revision/$filename"

    private fun LlmModelSpec.cacheKey(): String =
        "$repoId/$revision"
            .replace('/', '_')
            .replace(':', '_')

    private fun verifySha256IfNeeded(model: LlmModelSpec, file: File) {
        val expected = model.expectedSha256 ?: return
        val actual = file.sha256()
        if (!actual.equals(expected, ignoreCase = true)) {
            file.delete()
            throw IllegalStateException("Model checksum mismatch")
        }
    }

    private fun File.sha256(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val TAG = "LlmHelper"
    }
}
