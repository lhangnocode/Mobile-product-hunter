package android.app.producthunt.data.repository

import android.app.producthunt.data.local.db.dao.AgentConversationDao
import android.app.producthunt.data.local.db.entity.AgentConversationEntity
import android.app.producthunt.data.local.db.entity.AgentMessageEntity
import android.app.producthunt.data.local.db.entity.AgentMessageRole
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgentConversationRepository @Inject constructor(
    private val dao: AgentConversationDao,
) {
    fun observeConversations(): Flow<List<AgentConversationEntity>> =
        dao.observeConversations()

    fun observeMessages(conversationId: String): Flow<List<AgentMessageEntity>> =
        dao.observeMessages(conversationId)

    suspend fun getMessages(conversationId: String): List<AgentMessageEntity> =
        dao.getMessages(conversationId)

    suspend fun getConversation(conversationId: String): AgentConversationEntity? =
        dao.getConversation(conversationId)

    suspend fun createConversation(
        agentId: String,
        title: String? = null,
        now: Long = System.currentTimeMillis(),
    ): AgentConversationEntity {
        val conversation = AgentConversationEntity(
            id = UUID.randomUUID().toString(),
            agentId = agentId,
            title = title,
            createdAt = now,
            updatedAt = now,
        )
        dao.upsertConversation(conversation)
        return conversation
    }

    suspend fun upsertConversation(conversation: AgentConversationEntity) {
        dao.upsertConversation(conversation)
    }

    suspend fun appendUserMessage(
        conversationId: String,
        text: String,
        createdAt: Long = System.currentTimeMillis(),
    ): AgentMessageEntity =
        appendMessage(conversationId, AgentMessageRole.USER, text, createdAt)

    suspend fun appendModelMessage(
        conversationId: String,
        text: String,
        createdAt: Long = System.currentTimeMillis(),
    ): AgentMessageEntity =
        appendMessage(conversationId, AgentMessageRole.MODEL, text, createdAt)

    suspend fun appendToolMessage(
        conversationId: String,
        text: String,
        createdAt: Long = System.currentTimeMillis(),
    ): AgentMessageEntity =
        appendMessage(conversationId, AgentMessageRole.TOOL, text, createdAt)

    suspend fun appendMessage(
        conversationId: String,
        role: AgentMessageRole,
        text: String,
        createdAt: Long = System.currentTimeMillis(),
    ): AgentMessageEntity {
        val message = AgentMessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            role = role,
            text = text,
            createdAt = createdAt,
        )
        dao.appendMessage(message)
        return message
    }

    suspend fun deleteConversation(conversationId: String) {
        dao.deleteConversation(conversationId)
    }
}
