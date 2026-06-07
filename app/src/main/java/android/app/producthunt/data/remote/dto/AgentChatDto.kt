package android.app.producthunt.data.remote.dto

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class AgentChatRequest(
    @SerializedName("message") val message: String,
    @SerializedName("history") val history: List<AgentChatHistoryItem> = emptyList(),
    @SerializedName("context") val context: AgentChatContext = AgentChatContext(),
    @SerializedName("include_tool_trace") val includeToolTrace: Boolean = false,
)

data class AgentChatHistoryItem(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String,
)

data class AgentChatContext(
    @SerializedName("active_tab") val activeTab: String = "user_chatbot:search",
    @SerializedName("search_query") val searchQuery: String? = null,
    @SerializedName("product_id") val productId: String? = null,
    @SerializedName("shop_id") val shopId: Int? = null,
)

data class AgentChatResponse(
    @SerializedName("answer") val answer: String,
    @SerializedName("recommendations") val recommendations: List<JsonElement> = emptyList(),
    @SerializedName("sources") val sources: List<JsonElement> = emptyList(),
    @SerializedName("tool_trace") val toolTrace: List<JsonElement> = emptyList(),
    @SerializedName("handoff_required") val handoffRequired: Boolean = false,
    @SerializedName("alternatives") val alternatives: List<JsonElement> = emptyList(),
    @SerializedName("objection_answers") val objectionAnswers: List<JsonElement> = emptyList(),
    @SerializedName("urgency_cues") val urgencyCues: List<JsonElement> = emptyList(),
    @SerializedName("disclaimer") val disclaimer: String? = null,
)
