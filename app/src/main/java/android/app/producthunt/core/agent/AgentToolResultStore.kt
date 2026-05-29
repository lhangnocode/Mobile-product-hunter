package android.app.producthunt.core.agent

import android.app.producthunt.core.log.ILog
import com.google.gson.Gson

data class AgentToolResult(
    val name: String,
    val payload: String,
)

object AgentToolResultStore {
    private const val TAG = "AgentToolResultStore"

    private val gson = Gson()
    private val lock = Any()
    private val results = mutableListOf<AgentToolResult>()

    fun clear() {
        synchronized(lock) {
            results.clear()
        }
    }

    fun record(name: String, value: Any) {
        recordPayload(name, gson.toJson(value))
    }

    fun recordPayload(name: String, payload: String) {
        synchronized(lock) {
            results += AgentToolResult(
                name = name,
                payload = payload,
            )
        }
        ILog.d(TAG, "recordPayload", "name=$name", "payloadLength=${payload.length}")
    }

    fun drain(): List<AgentToolResult> =
        synchronized(lock) {
            val drained = results.toList()
            results.clear()
            drained
        }
}
