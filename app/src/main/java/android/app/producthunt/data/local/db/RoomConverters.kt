package android.app.producthunt.data.local.db

import android.app.producthunt.data.local.db.entity.AgentMessageRole
import androidx.room.TypeConverter

class RoomConverters {
    @TypeConverter
    fun fromAgentMessageRole(role: AgentMessageRole): String = role.name

    @TypeConverter
    fun toAgentMessageRole(value: String): AgentMessageRole = AgentMessageRole.valueOf(value)
}
