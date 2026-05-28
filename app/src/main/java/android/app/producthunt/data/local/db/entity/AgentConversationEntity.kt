package android.app.producthunt.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agent_conversations")
data class AgentConversationEntity(
    @PrimaryKey val id: String,
    val agentId: String,
    val title: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
