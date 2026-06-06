package android.app.producthunt.data.local.db.dao

import android.app.producthunt.data.local.db.entity.AgentConversationEntity
import android.app.producthunt.data.local.db.entity.AgentMessageEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentConversationDao {
    @Query("SELECT * FROM agent_conversations ORDER BY updatedAt DESC")
    fun observeConversations(): Flow<List<AgentConversationEntity>>

    @Query("SELECT * FROM agent_conversations WHERE id = :conversationId LIMIT 1")
    suspend fun getConversation(conversationId: String): AgentConversationEntity?

    @Query("SELECT * FROM agent_messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun observeMessages(conversationId: String): Flow<List<AgentMessageEntity>>

    @Query("SELECT * FROM agent_messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    suspend fun getMessages(conversationId: String): List<AgentMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertConversation(conversation: AgentConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AgentMessageEntity)

    @Query("UPDATE agent_conversations SET updatedAt = :updatedAt WHERE id = :conversationId")
    suspend fun touchConversation(conversationId: String, updatedAt: Long)

    @Query("DELETE FROM agent_messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)

    @Query("DELETE FROM agent_conversations WHERE id = :conversationId")
    suspend fun deleteConversationRow(conversationId: String)

    @Transaction
    suspend fun appendMessage(message: AgentMessageEntity) {
        insertMessage(message)
        touchConversation(message.conversationId, message.createdAt)
    }

    @Transaction
    suspend fun deleteConversation(conversationId: String) {
        deleteMessagesForConversation(conversationId)
        deleteConversationRow(conversationId)
    }
}
