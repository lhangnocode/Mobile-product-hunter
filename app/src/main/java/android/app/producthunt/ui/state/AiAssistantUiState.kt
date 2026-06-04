package android.app.producthunt.ui.state

data class AiAssistantUiState(
    val messages: List<AiAssistantMessage> = emptyList(),
    val isSending: Boolean = false,
    val errorMessage: String? = null,
)

data class AiAssistantMessage(
    val text: String,
    val isUser: Boolean,
    val isLoading: Boolean = false,
)
