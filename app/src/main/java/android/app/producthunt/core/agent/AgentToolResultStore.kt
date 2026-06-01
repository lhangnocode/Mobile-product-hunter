package android.app.producthunt.core.agent

import android.app.producthunt.core.log.ILog
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class AgentToolResult(
    val name: String,
    val payload: String,
)

data class AgentToolStarted(
    val name: String,
    val input: String,
)

object AgentToolResultStore {
    private const val TAG = "AgentToolResultStore"

    private val gson = Gson()
    private val lock = Any()
    private val results = mutableListOf<AgentToolResult>()
    private val _startedEvents = MutableSharedFlow<AgentToolStarted>(
        extraBufferCapacity = 32,
    )

    val startedEvents: SharedFlow<AgentToolStarted> = _startedEvents.asSharedFlow()

    fun clear() {
        synchronized(lock) {
            results.clear()
        }
    }

    fun record(name: String, value: Any) {
        recordPayload(name, gson.toJson(value))
    }

    fun recordStarted(name: String, input: String) {
        _startedEvents.tryEmit(AgentToolStarted(name, input))
        ILog.d(TAG, "recordStarted", "name=$name", input)
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
