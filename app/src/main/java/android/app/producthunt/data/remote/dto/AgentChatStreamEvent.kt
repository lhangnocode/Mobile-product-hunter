package android.app.producthunt.data.remote.dto

sealed interface AgentChatStreamEvent {
    data object Started : AgentChatStreamEvent
    data class Status(val text: String) : AgentChatStreamEvent
    data class Delta(val text: String) : AgentChatStreamEvent
    data class Completed(val answer: String? = null) : AgentChatStreamEvent
}
