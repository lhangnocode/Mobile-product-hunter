package android.app.producthunt.ui.viewmodel

import android.app.producthunt.core.log.ILog
import android.app.producthunt.data.local.db.entity.AgentConversationEntity
import android.app.producthunt.data.repository.AgentConversationRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgentConversationHistoryViewModel @Inject constructor(
    private val conversationRepository: AgentConversationRepository,
) : ViewModel() {
    private val hiddenConversationIds = MutableStateFlow<Set<String>>(emptySet())

    val conversations: StateFlow<List<AgentConversationEntity>> =
        combine(
            conversationRepository.observeConversations(),
            hiddenConversationIds,
        ) { conversations, hiddenIds ->
            conversations.filterNot { it.id in hiddenIds }
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteConversation(conversationId: String) {
        hiddenConversationIds.update { it + conversationId }
        viewModelScope.launch {
            runCatching {
                conversationRepository.deleteConversation(conversationId)
            }.onFailure { error ->
                hiddenConversationIds.update { it - conversationId }
                ILog.e(TAG, "deleteConversation", "failed", conversationId, throwable = error)
            }
        }
    }

    private companion object {
        private const val TAG = "AgentConversationHistoryViewModel"
    }
}
