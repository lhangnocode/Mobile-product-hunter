package android.app.producthunt.data.repository

import android.app.producthunt.core.state.UiState
import android.app.producthunt.data.remote.ApiErrorParser
import android.app.producthunt.data.remote.api.AgentApiService
import android.app.producthunt.data.remote.dto.AgentChatContext
import android.app.producthunt.data.remote.dto.AgentChatHistoryItem
import android.app.producthunt.data.remote.dto.AgentChatRequest
import android.app.producthunt.data.remote.dto.AgentChatResponse
import javax.inject.Inject

class AgentChatRepository @Inject constructor(
    private val api: AgentApiService,
) {
    suspend fun chat(
        message: String,
        history: List<AgentChatHistoryItem>,
        context: AgentChatContext = AgentChatContext(),
    ): UiState<AgentChatResponse> = try {
        UiState.Success(
            api.chat(
                AgentChatRequest(
                    message = message.take(MAX_MESSAGE_LENGTH),
                    history = history.takeLast(MAX_HISTORY_ITEMS),
                    context = context,
                    includeToolTrace = false,
                ),
            ),
        )
    } catch (e: Exception) {
        UiState.Error(ApiErrorParser.messageFrom(e, "Agent API request failed"))
    }

    private companion object {
        const val MAX_MESSAGE_LENGTH = 4000
        const val MAX_HISTORY_ITEMS = 20
    }
}
