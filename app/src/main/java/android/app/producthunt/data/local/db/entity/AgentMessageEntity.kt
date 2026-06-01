package android.app.producthunt.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "agent_messages",
    foreignKeys = [
        ForeignKey(
            entity = AgentConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index("conversationId"),
        Index(value = ["conversationId", "createdAt"]),
    ],
)
data class AgentMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val role: AgentMessageRole,
    val text: String,
    val createdAt: Long,
)
