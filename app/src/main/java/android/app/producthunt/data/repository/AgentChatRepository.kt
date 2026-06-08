package android.app.producthunt.data.repository

import android.app.producthunt.core.state.UiState
import android.app.producthunt.data.remote.ApiErrorParser
import android.app.producthunt.data.remote.api.AgentApiService
import android.app.producthunt.data.remote.dto.AgentChatContext
import android.app.producthunt.data.remote.dto.AgentChatHistoryItem
import android.app.producthunt.data.remote.dto.AgentChatRequest
import android.app.producthunt.data.remote.dto.AgentChatResponse
import android.app.producthunt.data.remote.dto.AgentChatStreamEvent
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.ResponseBody
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

    fun chatStream(
        message: String,
        history: List<AgentChatHistoryItem>,
        context: AgentChatContext = AgentChatContext(),
    ): Flow<AgentChatStreamEvent> = flow {
        emit(AgentChatStreamEvent.Started)

        val responseBody = api.chatStream(
            AgentChatRequest(
                message = message.take(MAX_MESSAGE_LENGTH),
                history = history.takeLast(MAX_HISTORY_ITEMS),
                context = context,
                includeToolTrace = false,
            ),
        )

        responseBody.use { body ->
            emitAll(body.readAgentChatStreamEvents())
        }
    }.flowOn(Dispatchers.IO)

    private companion object {
        const val MAX_MESSAGE_LENGTH = 4000
        const val MAX_HISTORY_ITEMS = 20
    }
}

private fun ResponseBody.readAgentChatStreamEvents(): Flow<AgentChatStreamEvent> = flow {
    val source = source()
    val dataLines = StringBuilder()
    var eventName: String? = null

    suspend fun flushEvent() {
        val payload = dataLines.toString().trim()
        if (payload.isNotBlank()) {
            parseAgentChatStreamPayload(payload, eventName).let { event ->
                when (event) {
                    is ParsedAgentChatStreamEvent.Status -> emit(AgentChatStreamEvent.Status(event.text))
                    is ParsedAgentChatStreamEvent.Delta -> emit(AgentChatStreamEvent.Delta(event.text))
                    is ParsedAgentChatStreamEvent.Completed -> emit(AgentChatStreamEvent.Completed(event.answer))
                    ParsedAgentChatStreamEvent.Ignore -> Unit
                }
            }
        }
        dataLines.clear()
        eventName = null
    }

    while (!source.exhausted()) {
        val line = source.readUtf8Line() ?: break

        when {
            line.isEmpty() -> flushEvent()
            line.startsWith("data:") -> {
                if (dataLines.isNotEmpty()) dataLines.append('\n')
                dataLines.append(line.removePrefix("data:").trimStart())
            }
            line.startsWith("event:") -> {
                eventName = line.removePrefix("event:").trim()
            }
            line.startsWith(":") -> Unit
            else -> {
                if (dataLines.isNotEmpty()) dataLines.append('\n')
                dataLines.append(line.trim())
            }
        }
    }

    if (dataLines.isNotBlank()) {
        flushEvent()
    }
}

private sealed interface ParsedAgentChatStreamEvent {
    data class Status(val text: String) : ParsedAgentChatStreamEvent
    data class Delta(val text: String) : ParsedAgentChatStreamEvent
    data class Completed(val answer: String?) : ParsedAgentChatStreamEvent
    data object Ignore : ParsedAgentChatStreamEvent
}

private fun parseAgentChatStreamPayload(
    payload: String,
    eventName: String?,
): ParsedAgentChatStreamEvent {
    val trimmed = payload.trim()
    if (trimmed.isBlank() || trimmed == "[DONE]") return ParsedAgentChatStreamEvent.Completed(null)

    val json = runCatching { JsonParser.parseString(trimmed) }.getOrNull()
    if (json?.isJsonObject == true) {
        val obj = json.asJsonObject
        val explicitEvent = eventName?.lowercase()
            ?: obj.get("event")?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString?.lowercase()
            ?: obj.get("type")?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString?.lowercase()

        val token = firstString(obj, "token", "delta", "content", "text", "chunk")
        val answer = firstString(obj, "answer", "message")
        val toolName = firstString(obj, "name", "tool_name", "toolCallName", "tool")
            ?: nestedToolName(obj, "tool_call", "tool_calls", "tool_result", "tool_response")
        val toolLike = explicitEvent in setOf(
            "tool",
            "tool_call",
            "tool_calls",
            "tool_result",
            "tool_response",
            "tool_start",
            "tool_started",
            "tool_end",
            "tool_finished",
        ) || obj.has("tool_call") ||
            obj.has("tool_calls") ||
            obj.has("tool_trace") ||
            obj.has("tool_response") ||
            obj.has("tool_result") ||
            obj.has("tool_name")
        val done = firstBoolean(obj, "done", "is_done", "final") ||
            explicitEvent in setOf("done", "complete", "completed", "final", "end")

        when {
            !answer.isNullOrBlank() && done -> return ParsedAgentChatStreamEvent.Completed(answer)
            !answer.isNullOrBlank() -> return ParsedAgentChatStreamEvent.Completed(answer)
            toolLike -> return ParsedAgentChatStreamEvent.Status(
                friendlyToolStatus(toolName, explicitEvent)
            )
            !token.isNullOrBlank() -> return ParsedAgentChatStreamEvent.Delta(token)
            done -> return ParsedAgentChatStreamEvent.Completed(null)
            else -> return ParsedAgentChatStreamEvent.Ignore
        }
    }

    if (trimmed.startsWith("{") || trimmed.startsWith("[")) {
        return ParsedAgentChatStreamEvent.Ignore
    }

    return ParsedAgentChatStreamEvent.Delta(trimmed)
}

private fun friendlyToolStatus(toolName: String?, eventName: String?): String =
    when (toolName?.lowercase()) {
        "searchproducts", "search_products" -> "Searching products..."
        "getproductdetail", "get_product_detail" -> "Getting product details..."
        "gettrendingdeals", "get_trending_deals" -> "Checking deals..."
        "getproductpricerecords", "get_product_price_records" -> "Checking price history..."
        "analyzelistingprice", "analyze_listing_price" -> "Analyzing price..."
        else -> when (eventName) {
            "tool_call", "tool_calls", "tool_start", "tool_started" -> "Using tools..."
            "tool_result", "tool_response", "tool_end", "tool_finished" -> "Using tools..."
            else -> "Using tools..."
        }
    }

private fun firstString(obj: com.google.gson.JsonObject, vararg keys: String): String? =
    keys.firstNotNullOfOrNull { key ->
        obj.get(key)
            ?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }
            ?.asString
            ?.takeIf { it.isNotBlank() }
    }

private fun nestedToolName(obj: com.google.gson.JsonObject, vararg keys: String): String? =
    keys.firstNotNullOfOrNull { key ->
        val value = obj.get(key) ?: return@firstNotNullOfOrNull null
        when {
            value.isJsonObject -> firstString(value.asJsonObject, "name", "tool_name", "toolCallName", "tool")
            value.isJsonArray -> value.asJsonArray.firstNotNullOfOrNull { item ->
                item.takeIf { it.isJsonObject }
                    ?.asJsonObject
                    ?.let { firstString(it, "name", "tool_name", "toolCallName", "tool") }
            }
            else -> null
        }
    }

private fun firstBoolean(obj: com.google.gson.JsonObject, vararg keys: String): Boolean =
    keys.any { key ->
        obj.get(key)
            ?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isBoolean }
            ?.asBoolean == true
    }
