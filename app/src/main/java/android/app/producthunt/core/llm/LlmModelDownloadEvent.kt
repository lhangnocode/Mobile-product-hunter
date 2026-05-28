package android.app.producthunt.core.llm

sealed interface LlmModelDownloadEvent {
    data object Idle : LlmModelDownloadEvent

    data class Starting(val url: String) : LlmModelDownloadEvent

    data class Progress(
        val downloadedBytes: Long,
        val totalBytes: Long?,
        val percent: Float?,
    ) : LlmModelDownloadEvent

    data class Completed(val modelPath: String) : LlmModelDownloadEvent

    data class Failed(
        val message: String,
        val cause: Throwable? = null,
    ) : LlmModelDownloadEvent
}
