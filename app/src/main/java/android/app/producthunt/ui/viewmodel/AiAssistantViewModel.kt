package android.app.producthunt.ui.viewmodel

import android.app.producthunt.core.agent.AgentCallCallback
import android.app.producthunt.core.agent.AgentOrchestrator
import android.app.producthunt.core.log.ILog
import android.app.producthunt.core.state.UiState
import android.app.producthunt.data.local.db.entity.AgentMessageEntity
import android.app.producthunt.data.local.db.entity.AgentMessageRole
import android.app.producthunt.data.remote.dto.AgentChatContext
import android.app.producthunt.data.remote.dto.AgentChatHistoryItem
import android.app.producthunt.data.repository.AgentChatRepository
import android.app.producthunt.data.repository.AgentConversationRepository
import android.app.producthunt.ui.state.AiAssistantMode
import android.app.producthunt.ui.state.AiAssistantMessage
import android.app.producthunt.ui.state.AiAssistantUiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val conversationRepository: AgentConversationRepository,
    private val agentChatRepository: AgentChatRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AiAssistantUiState())
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    private var conversationId: String? = null

    fun sendQuery(query: String) {
        if (query.isBlank()) return

        val responseIndex = appendUserMessage(query)
        _uiState.update { it.copy(isSending = true, errorMessage = null) }

        if (_uiState.value.mode == AiAssistantMode.AGENT_API) {
            sendAgentApiQuery(query, responseIndex)
            return
        }

        sendOnDeviceQuery(query, responseIndex)
    }

    fun setMode(mode: AiAssistantMode) {
        _uiState.update { it.copy(mode = mode, errorMessage = null) }
    }

    private fun sendAgentApiQuery(query: String, responseIndex: Int) {
        val history = recentAgentApiHistory()
        viewModelScope.launch {
            updateMessage(responseIndex, "Thinking...", isLoading = true)
            when (
                val result = agentChatRepository.chat(
                    message = query,
                    history = history,
                    context = AgentChatContext(
                        activeTab = "user_chatbot:search",
                        searchQuery = query,
                    ),
                )
            ) {
                is UiState.Success -> {
                    val answer = result.data.answer.ifBlank { "No response from Agent API." }
                    updateMessage(responseIndex, answer, isLoading = false)
                    _uiState.update { it.copy(isSending = false) }
                }
                is UiState.Error -> {
                    updateMessage(responseIndex, result.message, isLoading = false)
                    _uiState.update { it.copy(isSending = false, errorMessage = result.message) }
                }
                else -> Unit
            }
        }
    }

    private fun sendOnDeviceQuery(query: String, responseIndex: Int) {
        viewModelScope.launch {
            AgentOrchestrator.performAgentCall(
                prompt = query,
                conversationId = conversationId,
                callback = object : AgentCallCallback {
                    override fun onConversationReady(conversationId: String) {
                        this@AiAssistantViewModel.conversationId = conversationId
                    }

                    override fun onStarted() {
                        updateMessage(responseIndex, "Thinking...", isLoading = true)
                    }

                    override fun onToolStarted(name: String, input: String) {
                        updateMessage(
                            index = responseIndex,
                            text = name.toToolStatusText(input),
                            isLoading = true,
                        )
                    }

                    override fun onMessage(text: String) {
                        updateMessage(responseIndex, text, isLoading = false)
                    }

                    override fun onToolResponse(name: String, payload: String) {
                        ILog.d(TAG, "onToolResponse", "recorded", name, "payloadLength=${payload.length}")
                    }

                    override fun onCompleted(text: String) {
                        updateMessage(responseIndex, text, isLoading = false)
                        _uiState.update { it.copy(isSending = false) }
                    }

                    override fun onFailed(message: String, cause: Throwable?) {
                        updateMessage(responseIndex, message, isLoading = false)
                        _uiState.update { it.copy(isSending = false, errorMessage = message) }
                    }
                },
            )
        }
    }

    fun startNewConversation() {
        conversationId = null
        _uiState.value = AiAssistantUiState()
    }

    fun loadConversation(conversationId: String) {
        if (conversationId.isBlank()) return

        viewModelScope.launch {
            val messages = conversationRepository.getMessages(conversationId)
            this@AiAssistantViewModel.conversationId = conversationId
            _uiState.value = AiAssistantUiState(
                messages = messages.toAiAssistantMessages(),
                mode = AiAssistantMode.ON_DEVICE,
            )

            AgentOrchestrator.switchConversation(conversationId)
                .onFailure { error ->
                    val message = error.message ?: "Unable to switch AI conversation"
                    ILog.e(TAG, "loadConversation", "switch failed", conversationId, throwable = error)
                    _uiState.update { it.copy(errorMessage = message) }
                }
        }
    }

    private fun appendUserMessage(query: String): Int {
        val userMessage = AiAssistantMessage(text = query, isUser = true)
        val assistant = AiAssistantMessage(text = "", isUser = false, isLoading = true)
        var responseIndex = 0
        _uiState.update { state ->
            val updated = state.messages + userMessage + assistant
            responseIndex = updated.lastIndex
            state.copy(messages = updated)
        }
        return responseIndex
    }

    private fun updateMessage(index: Int, text: String, isLoading: Boolean) {
        _uiState.update { state ->
            if (index !in state.messages.indices) return@update state
            val updated = state.messages.toMutableList()
            updated[index] = updated[index].copy(text = text, isLoading = isLoading)
            state.copy(messages = updated)
        }
    }

    private fun recentAgentApiHistory(): List<AgentChatHistoryItem> =
        _uiState.value.messages
            .dropLast(2)
            .filter { it.text.isNotBlank() && !it.isLoading }
            .takeLast(MAX_AGENT_HISTORY_ITEMS)
            .map {
                AgentChatHistoryItem(
                    role = if (it.isUser) "user" else "assistant",
                    content = it.text.take(MAX_AGENT_MESSAGE_LENGTH),
                )
            }

    private fun List<AgentMessageEntity>.toAiAssistantMessages(): List<AiAssistantMessage> =
        mapNotNull { message ->
            when (message.role) {
                AgentMessageRole.USER -> AiAssistantMessage(text = message.text, isUser = true)
                AgentMessageRole.MODEL -> AiAssistantMessage(text = message.text, isUser = false)
                AgentMessageRole.TOOL -> null
            }
        }

    private fun String.toToolStatusText(input: String): String =
        when (this) {
            "searchProducts" -> "Searching products..."
            "getProductDetail" -> "Getting product details..."
            "getTrendingDeals" -> "Checking deals..."
            "getProductPriceRecords" -> "Checking price history..."
            "analyzeListingPrice" -> "Analyzing price..."
            else -> "Using tool..."
        }.let { status ->
            if (input.isBlank()) status else "$status\n$input"
        }

    private companion object {
        private const val TAG = "AiAssistantViewModel"
        private const val MAX_AGENT_HISTORY_ITEMS = 20
        private const val MAX_AGENT_MESSAGE_LENGTH = 4000
    }
}
