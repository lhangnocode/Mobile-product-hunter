package android.app.producthunt.ui.viewmodel

import android.app.producthunt.core.agent.AgentCallCallback
import android.app.producthunt.core.agent.AgentOrchestrator
import android.app.producthunt.core.agent.AgentPriceAnalysisResult
import android.app.producthunt.core.agent.AgentPriceRecordsResult
import android.app.producthunt.core.agent.AgentProductListingsResult
import android.app.producthunt.core.agent.AgentProductSummary
import android.app.producthunt.core.agent.AgentSearchResult
import android.app.producthunt.core.agent.AgentTrendingItemSummary
import android.app.producthunt.core.agent.AgentTrendingResult
import android.app.producthunt.core.log.ILog
import android.app.producthunt.data.local.db.entity.AgentMessageRole
import android.app.producthunt.data.repository.AgentConversationRepository
import android.app.producthunt.ui.screens.main.ChatMessage
import android.app.producthunt.ui.state.AgentSearchUiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AgentSearchViewModel @Inject constructor(
    private val conversationRepository: AgentConversationRepository,
) : ViewModel() {
    private val gson = Gson()

    private val _uiState = MutableStateFlow(AgentSearchUiState())
    val uiState: StateFlow<AgentSearchUiState> = _uiState.asStateFlow()

    private var conversationId: String? = null

    fun sendQuery(query: String) {
        if (query.isBlank()) return

        val responseIndex = appendUserMessage(query)
        _uiState.update { it.copy(isSending = true, errorMessage = null) }

        viewModelScope.launch {
            val responseConversationId = conversationId
            AgentOrchestrator.performAgentCall(
                prompt = query,
                conversationId = responseConversationId,
                callback = object : AgentCallCallback {
                    override fun onConversationReady(conversationId: String) {
                        this@AgentSearchViewModel.conversationId = conversationId
                    }

                    override fun onStarted() {
                        updateMessage(responseIndex, "Thinking...", isLoading = true)
                    }

                    override fun onToolStarted(name: String, input: String) {
                        updateMessage(
                            index = responseIndex,
                            text = "${name.toToolStatusText()}\n$input",
                            isLoading = true,
                        )
                    }

                    override fun onMessage(text: String) {
                        updateMessage(responseIndex, text, isLoading = false)
                    }

                    override fun onToolResponse(name: String, payload: String) {
                        appendToolMessage(payload)
                    }

                    override fun onCompleted(text: String) {
                        updateMessage(responseIndex, text, isLoading = false)
                        loadToolMessages(this@AgentSearchViewModel.conversationId)
                        _uiState.update { it.copy(isSending = false) }
                    }

                    override fun onFailed(message: String, cause: Throwable?) {
                        updateMessage(responseIndex, message, isLoading = false)
                        _uiState.update { it.copy(isSending = false, errorMessage = message) }
                    }
                }
            )
        }
    }

    fun ensureConversationId(conversationId: String?) {
        if (!conversationId.isNullOrBlank()) {
            this.conversationId = conversationId
        }
    }

    private fun appendUserMessage(query: String): Int {
        val userMessage = ChatMessage(text = query, isUser = true)
        val assistant = ChatMessage(text = "", isUser = false, isLoading = true, showAgentHeader = true)
        var responseIndex = 0
        _uiState.update { state ->
            val updated = state.messages + userMessage + assistant
            responseIndex = updated.lastIndex
            state.copy(messages = updated)
        }
        return responseIndex
    }

    private fun updateMessage(index: Int, text: String, isLoading: Boolean) {
        _uiState.update { state ->
            if (index !in state.messages.indices) return@update state
            val updated = state.messages.toMutableList()
            val existing = updated[index]
            updated[index] = existing.copy(text = text, isLoading = isLoading)
            state.copy(messages = updated)
        }
    }

    private fun loadToolMessages(conversationId: String?) {
        val resolvedId = conversationId ?: this.conversationId
        if (resolvedId.isNullOrBlank()) return

        viewModelScope.launch {
            val history = conversationRepository.getMessages(resolvedId)
            val toolMessages = history
                .filter { it.role == AgentMessageRole.TOOL }
                .mapNotNull { parseToolMessage(it.text) }

            if (toolMessages.isNotEmpty()) {
                _uiState.update { state ->
                    val existing = state.messages
                    val newMessages = toolMessages.filterNot { message ->
                        existing.any { existingMessage -> existingMessage.hasSameAgentPayload(message) }
                    }
                    state.copy(messages = state.messages + newMessages)
                }
            }
        }
    }

    private fun appendToolMessage(payload: String) {
        val message = parseToolMessage(payload) ?: return
        _uiState.update { state ->
            if (state.messages.any { it.hasSameAgentPayload(message) }) {
                state
            } else {
                state.copy(messages = state.messages + message)
            }
        }
    }

    private fun parseToolMessage(text: String): ChatMessage? =
        parseSearchResult(text)
            ?: parseTrendingResult(text)
            ?: parseProductListingsResult(text)
            ?: parsePriceRecordsResult(text)
            ?: parsePriceAnalysisResult(text)
            ?: run {
                ILog.w(TAG, "parseToolMessage", "unsupported tool payload")
                null
            }

    private fun parseSearchResult(text: String): ChatMessage? =
        runCatching {
            val result = gson.fromJson(text, AgentSearchResult::class.java)
            val products = result.products.orEmpty()
            result.takeIf { products.isNotEmpty() || it.error != null }?.let {
                ChatMessage(
                    text = it.error ?: "Tôi tìm thấy ${it.totalResults} sản phẩm:",
                    isUser = false,
                    agentProductList = products,
                    showAgentHeader = true,
                )
            }
        }.getOrNull()

    private fun parseTrendingResult(text: String): ChatMessage? =
        runCatching {
            val result = gson.fromJson(text, AgentTrendingResult::class.java)
            val items = result.items.orEmpty()
            result.takeIf { items.isNotEmpty() || it.error != null }?.let {
                ChatMessage(
                    text = it.error ?: "Deals hot hôm nay:",
                    isUser = false,
                    agentTrendingItems = items,
                    showAgentHeader = true,
                )
            }
        }.getOrNull()

    private fun parseProductListingsResult(text: String): ChatMessage? =
        runCatching {
            val result = gson.fromJson(text, AgentProductListingsResult::class.java)
            result.takeIf { it.listings.orEmpty().isNotEmpty() || it.error != null }?.let {
                ChatMessage(
                    text = it.error ?: summarizeProductListingsResult(it),
                    isUser = false,
                    showAgentHeader = true,
                )
            }
        }.getOrNull()

    private fun parsePriceAnalysisResult(text: String): ChatMessage? =
        runCatching {
            val result = gson.fromJson(text, AgentPriceAnalysisResult::class.java)
            result.takeIf { it.analysis != null || it.error != null }?.let {
                ChatMessage(
                    text = it.error ?: summarizePriceAnalysisResult(it),
                    isUser = false,
                    showAgentHeader = true,
                )
            }
        }.getOrNull()

    private fun parsePriceRecordsResult(text: String): ChatMessage? =
        runCatching {
            val result = gson.fromJson(text, AgentPriceRecordsResult::class.java)
            result.takeIf { it.recentRecords.orEmpty().isNotEmpty() || it.error != null }?.let {
                ChatMessage(
                    text = it.error ?: summarizePriceRecordsResult(it),
                    isUser = false,
                    showAgentHeader = true,
                )
            }
        }.getOrNull()

    private fun summarizeProductListingsResult(result: AgentProductListingsResult): String {
        result.error?.let { return it }
        val listings = result.listings.orEmpty()
        if (listings.isEmpty()) return "Không tìm thấy listing cho sản phẩm này."

        val best = listings.minByOrNull { it.currentPrice ?: Double.MAX_VALUE } ?: listings.first()
        val price = best.currentPrice?.formatVnd() ?: "chưa có giá"
        return "Tôi tìm thấy ${result.totalResults} listing. Giá tốt nhất hiện là $price tại ${best.platformName}."
    }

    private fun summarizePriceAnalysisResult(result: AgentPriceAnalysisResult): String {
        result.error?.let { return it }
        val analysis = result.analysis ?: return "Chưa có dữ liệu phân tích giá."
        val label = analysis.label ?: analysis.status ?: "chưa rõ mức độ tốt"
        val low = analysis.lowestEverPrice?.let { ", thấp nhất từng ghi nhận ${it.formatVnd()}" }.orEmpty()
        return "Giá hiện tại ${analysis.currentPrice.formatVnd()} được đánh giá: $label$low."
    }

    private fun summarizePriceRecordsResult(result: AgentPriceRecordsResult): String {
        result.error?.let { return it }
        val latest = result.latest?.price?.formatVnd() ?: "chưa có giá mới nhất"
        val low = result.lowestPrice?.formatVnd() ?: "chưa rõ"
        val high = result.highestPrice?.formatVnd() ?: "chưa rõ"
        return "Lịch sử giá có ${result.totalRecords} bản ghi. Giá mới nhất $latest, thấp nhất $low, cao nhất $high."
    }

    private fun Double.formatVnd(): String =
        "%,.0f đ".format(this)

    private fun String.toToolStatusText(): String =
        when (this) {
            "searchProducts" -> "Searching products..."
            "getProductDetail" -> "Getting product detail..."
            "getTrendingDeals" -> "Checking trending deals..."
            "getProductPriceRecords" -> "Checking price records..."
            "analyzeListingPrice" -> "Analyzing listing price..."
            else -> "Using tool: $this..."
        }

    private fun ChatMessage.hasSameAgentPayload(other: ChatMessage): Boolean =
        text == other.text &&
            agentProductList.sameProducts(other.agentProductList) &&
            agentTrendingItems.sameTrendingItems(other.agentTrendingItems)

    private fun List<AgentProductSummary>.sameProducts(other: List<AgentProductSummary>): Boolean =
        map { it.id to it.productName } == other.map { it.id to it.productName }

    private fun List<AgentTrendingItemSummary>.sameTrendingItems(other: List<AgentTrendingItemSummary>): Boolean =
        map { it.id to it.productName } == other.map { it.id to it.productName }

    private companion object {
        private const val TAG = "AgentSearchViewModel"
    }
}
