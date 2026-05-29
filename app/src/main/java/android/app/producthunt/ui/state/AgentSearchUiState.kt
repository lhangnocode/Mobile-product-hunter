package android.app.producthunt.ui.state

import android.app.producthunt.ui.screens.main.ChatMessage

data class AgentSearchUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isSending: Boolean = false,
    val errorMessage: String? = null,
)
