package android.app.producthunt.data.local.db

import android.app.producthunt.data.local.db.dao.AgentConversationDao
import android.app.producthunt.data.local.db.entity.AgentConversationEntity
import android.app.producthunt.data.local.db.entity.AgentMessageEntity
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        AgentConversationEntity::class,
        AgentMessageEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(RoomConverters::class)
abstract class ProductHuntDatabase : RoomDatabase() {
    abstract fun agentConversationDao(): AgentConversationDao
}
