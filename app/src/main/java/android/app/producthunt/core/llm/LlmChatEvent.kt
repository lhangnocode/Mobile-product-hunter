package android.app.producthunt.core.llm

sealed interface LlmChatEvent {
    data object Started : LlmChatEvent
    data class Message(val text: String) : LlmChatEvent
    data class ToolResponse(val name: String, val payload: String) : LlmChatEvent
    data class Completed(val text: String) : LlmChatEvent
    data class Failed(
        val message: String,
        val cause: Throwable? = null,
    ) : LlmChatEvent
}

data class LlmToolResponse(
    val name: String,
    val payload: String,
)
