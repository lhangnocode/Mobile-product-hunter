package android.app.producthunt.ui.viewmodel

import android.app.producthunt.core.agent.AgentCallCallback
import android.app.producthunt.core.agent.AgentCompareItemSummary
import android.app.producthunt.core.agent.AgentOrchestrator
import android.app.producthunt.core.agent.AgentCompareResult
import android.app.producthunt.core.agent.AgentPriceAnalysisResult
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
            var toolSummaryText: String? = null
            AgentOrchestrator.performAgentCall(
                prompt = query,
                conversationId = responseConversationId,
                callback = object : AgentCallCallback {
                    override fun onConversationReady(conversationId: String) {
                        this@AgentSearchViewModel.conversationId = conversationId
                    }

                    override fun onMessage(text: String) {
                        updateMessage(responseIndex, text, isLoading = false)
                    }

                    override fun onToolResponse(name: String, payload: String) {
                        summarizeToolPayload(payload)?.let { summary ->
                            toolSummaryText = summary
                            updateMessage(responseIndex, summary, isLoading = false)
                        }
                        appendToolMessage(payload)
                    }

                    override fun onCompleted(text: String) {
                        val resolvedText = if (
                            toolSummaryText != null &&
                            (text.isBlank() || text.looksLikeNoToolDataAnswer())
                        ) {
                            toolSummaryText.orEmpty()
                        } else {
                            text
                        }
                        updateMessage(responseIndex, resolvedText, isLoading = false)
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
            ?: parseCompareResult(text)
            ?: parseTrendingResult(text)
            ?: parseProductListingsResult(text)
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

    private fun parseCompareResult(text: String): ChatMessage? =
        runCatching {
            val result = gson.fromJson(text, AgentCompareResult::class.java)
            val items = result.items.orEmpty()
            result.takeIf { items.isNotEmpty() || it.error != null }?.let {
                ChatMessage(
                    text = it.error ?: "So sánh ${it.totalResults} sản phẩm:",
                    isUser = false,
                    agentCompareItems = items,
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

    private fun summarizeToolPayload(text: String): String? =
        runCatching { gson.fromJson(text, AgentSearchResult::class.java) }
            .getOrNull()
            ?.takeIf { it.products.orEmpty().isNotEmpty() || it.error != null }
            ?.let { summarizeSearchResult(it) }
            ?: runCatching { gson.fromJson(text, AgentCompareResult::class.java) }
                .getOrNull()
                ?.takeIf { it.items.orEmpty().isNotEmpty() || it.error != null }
                ?.let { summarizeCompareResult(it) }
            ?: runCatching { gson.fromJson(text, AgentTrendingResult::class.java) }
                .getOrNull()
                ?.takeIf { it.items.orEmpty().isNotEmpty() || it.error != null }
                ?.let { summarizeTrendingResult(it) }
            ?: runCatching { gson.fromJson(text, AgentProductListingsResult::class.java) }
                .getOrNull()
                ?.takeIf { it.listings.orEmpty().isNotEmpty() || it.error != null }
                ?.let { summarizeProductListingsResult(it) }
            ?: runCatching { gson.fromJson(text, AgentPriceAnalysisResult::class.java) }
                .getOrNull()
                ?.takeIf { it.analysis != null || it.error != null }
                ?.let { summarizePriceAnalysisResult(it) }

    private fun summarizeSearchResult(result: AgentSearchResult): String {
        result.error?.let { return it }
        val products = result.products.orEmpty()
        if (products.isEmpty()) return "Không tìm thấy sản phẩm phù hợp cho \"${result.keyword}\"."

        val topProducts = products.take(3).joinToString(separator = "\n") { product ->
            "- ${product.productName ?: "Sản phẩm"}${product.brand?.let { " ($it)" } ?: ""}"
        }
        return "Tôi tìm thấy ${result.totalResults} sản phẩm cho \"${result.keyword}\".\n$topProducts"
    }

    private fun summarizeCompareResult(result: AgentCompareResult): String {
        result.error?.let { return it }
        val items = result.items.orEmpty()
        if (items.isEmpty()) return "Không tìm thấy dữ liệu so sánh cho \"${result.keyword}\"."

        val best = items.minByOrNull { it.lowestPrice ?: Double.MAX_VALUE } ?: items.first()
        val bestPrice = best.lowestPrice?.formatVnd() ?: "chưa có giá"
        val savings = best.estimatedSavings
            ?.takeIf { it > 0.0 }
            ?.let { ", chênh lệch khoảng ${it.formatVnd()}" }
            .orEmpty()

        return "Tôi đã lấy dữ liệu giá cho \"${result.keyword}\". Lựa chọn rẻ nhất hiện thấy là ${best.productName} tại ${best.bestPlatform ?: "một nền tảng"} với giá $bestPrice$savings. Tôi cũng hiển thị các lựa chọn so sánh bên dưới."
    }

    private fun summarizeTrendingResult(result: AgentTrendingResult): String {
        result.error?.let { return it }
        val items = result.items.orEmpty()
        if (items.isEmpty()) return "Chưa có deal trending phù hợp."

        val top = items.first()
        val discount = top.discountPercent?.let { " giảm ${it.toInt()}%" }.orEmpty()
        return "Deal nổi bật: ${top.productName} tại ${top.platformName ?: "một nền tảng"} giá ${top.currentPrice.formatVnd()}$discount. Tôi hiển thị thêm các deal bên dưới."
    }

    private fun summarizeProductListingsResult(result: AgentProductListingsResult): String {
        result.error?.let { return it }
        val listings = result.listings.orEmpty()
        if (listings.isEmpty()) return "Không tìm thấy listing cho sản phẩm này."

        val best = listings.minByOrNull { it.listing.currentPrice ?: Double.MAX_VALUE } ?: listings.first()
        val price = best.listing.currentPrice?.formatVnd() ?: "chưa có giá"
        val label = best.analysis?.label?.let { " ($it)" }.orEmpty()
        return "Tôi tìm thấy ${result.totalResults} listing. Giá tốt nhất hiện là $price tại ${best.listing.platformName}$label."
    }

    private fun summarizePriceAnalysisResult(result: AgentPriceAnalysisResult): String {
        result.error?.let { return it }
        val analysis = result.analysis ?: return "Chưa có dữ liệu phân tích giá."
        val label = analysis.label ?: analysis.status ?: "chưa rõ mức độ tốt"
        val low = analysis.lowestEverPrice?.let { ", thấp nhất từng ghi nhận ${it.formatVnd()}" }.orEmpty()
        return "Giá hiện tại ${analysis.currentPrice.formatVnd()} được đánh giá: $label$low."
    }

    private fun String.looksLikeNoToolDataAnswer(): Boolean {
        val normalized = lowercase()
        return listOf(
            "couldn't access",
            "could not access",
            "can't access",
            "cannot access",
            "don't have access",
            "do not have access",
            "real-time pricing",
            "real time pricing",
            "availability data",
            "future or unreleased",
            "không thể truy cập",
            "không có dữ liệu",
        ).any { it in normalized }
    }

    private fun Double.formatVnd(): String =
        "%,.0f đ".format(this)

    private fun ChatMessage.hasSameAgentPayload(other: ChatMessage): Boolean =
        text == other.text &&
            agentProductList.sameProducts(other.agentProductList) &&
            agentCompareItems.sameCompareItems(other.agentCompareItems) &&
            agentTrendingItems.sameTrendingItems(other.agentTrendingItems)

    private fun List<AgentProductSummary>.sameProducts(other: List<AgentProductSummary>): Boolean =
        map { it.id to it.productName } == other.map { it.id to it.productName }

    private fun List<AgentCompareItemSummary>.sameCompareItems(other: List<AgentCompareItemSummary>): Boolean =
        map { it.id to it.productName } == other.map { it.id to it.productName }

    private fun List<AgentTrendingItemSummary>.sameTrendingItems(other: List<AgentTrendingItemSummary>): Boolean =
        map { it.id to it.productName } == other.map { it.id to it.productName }

    private companion object {
        private const val TAG = "AgentSearchViewModel"
    }
}
