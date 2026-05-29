package android.app.producthunt.core.agent

import android.app.producthunt.core.state.UiState
import android.app.producthunt.data.remote.dto.PlatformListingDto
import android.app.producthunt.data.remote.dto.PriceAnalysisResponse
import android.app.producthunt.data.repository.PlatformProductRepository
import android.app.producthunt.data.repository.PriceRecordRepository
import android.app.producthunt.data.repository.ProductRepository
import com.google.ai.edge.litertlm.Tool
import com.google.ai.edge.litertlm.ToolParam
import com.google.ai.edge.litertlm.ToolSet
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class AgentToolSet(
    private val productRepository: ProductRepository,
    private val platformProductRepository: PlatformProductRepository,
    private val priceRecordRepository: PriceRecordRepository,
) : ToolSet {
    private val gson = Gson()

    private companion object {
        private const val MAX_TOOL_ITEMS = 6
        private const val MAX_ANALYZED_LISTINGS = 3
    }

    @Tool(
        description = "Search canonical products by keyword. Use this for broad discovery, categories, budgets, and unclear product names before recommending products."
    )
    fun searchProducts(
        @ToolParam(description = "Product keyword, category, brand, or user phrase. Minimum 2 characters.") query: String,
        @ToolParam(description = "Page number (1-based).") page: Int = 1,
        @ToolParam(description = "Number of products to return. Keep small for concise answers.") limit: Int = MAX_TOOL_ITEMS,
    ): String = (when (val result = runBlocking(Dispatchers.IO) {
        productRepository.search(query, page, limit.coerceIn(1, MAX_TOOL_ITEMS))
    }) {
        is UiState.Success -> AgentSearchResult(
            keyword = result.data.keyword,
            totalResults = result.data.totalResults,
            products = result.data.data
                .take(MAX_TOOL_ITEMS)
                .map { it.toSummary() },
        )
        is UiState.Error -> AgentSearchResult(
            keyword = query,
            totalResults = 0,
            products = emptyList(),
            error = result.message,
        )
        else -> AgentSearchResult(
            keyword = query,
            totalResults = 0,
            products = emptyList(),
            error = "Search not ready",
        )
    }).recordToolResult("searchProducts")

    @Tool(
        description = "Compare matching products with platform listings and prices. Use this for best price, where to buy, compare, vs, Shopee/Lazada/Tiki, and platform availability questions."
    )
    fun compareProductPrices(
        @ToolParam(description = "Product or comparison query. Examples: 'iPhone 15', 'Samsung Tab S10 vs iPad Mini'.") query: String,
    ): String = (when (val result = runBlocking(Dispatchers.IO) {
        productRepository.compare(query)
    }) {
        is UiState.Success -> AgentCompareResult(
            keyword = result.data.keyword,
            totalResults = result.data.totalResults,
            items = result.data.data
                .take(MAX_TOOL_ITEMS)
                .map { it.toSummary() },
        )
        is UiState.Error -> AgentCompareResult(
            keyword = query,
            totalResults = 0,
            items = emptyList(),
            error = result.message,
        )
        else -> AgentCompareResult(
            keyword = query,
            totalResults = 0,
            items = emptyList(),
            error = "Compare not ready",
        )
    }).recordToolResult("compareProductPrices")

    @Tool(
        description = "Get current trending deals sorted by discount magnitude. Use this for hot deals, today's deals, discounts, and bargain hunting."
    )
    fun getTrendingDeals(
        @ToolParam(description = "Maximum number of deals to return.") limit: Int = MAX_TOOL_ITEMS,
    ): String = (when (val result = runBlocking(Dispatchers.IO) {
        platformProductRepository.getTrending(limit.coerceIn(1, MAX_TOOL_ITEMS))
    }) {
        is UiState.Success -> AgentTrendingResult(
            totalResults = result.data.size,
            items = result.data
                .take(MAX_TOOL_ITEMS)
                .map { it.toSummary() },
        )
        is UiState.Error -> AgentTrendingResult(
            totalResults = 0,
            items = emptyList(),
            error = result.message,
        )
        else -> AgentTrendingResult(
            totalResults = 0,
            items = emptyList(),
            error = "Trending not ready",
        )
    }).recordToolResult("getTrendingDeals")

    @Tool(
        description = "Inspect marketplace listings for a canonical product id and analyze the best few listing prices against price history. Use after searchProducts when the user picks a product or asks if a product has a good deal."
    )
    fun inspectProductDeals(
        @ToolParam(description = "Canonical product UUID returned by searchProducts or compareProductPrices.") productId: String,
        @ToolParam(description = "Maximum number of marketplace listings to return.") limit: Int = MAX_TOOL_ITEMS,
    ): String = (when (val result = runBlocking(Dispatchers.IO) {
        platformProductRepository.getListingsByProductId(productId)
    }) {
        is UiState.Success -> {
            val listings = result.data
                .sortedBy { it.currentPrice.toDoubleOrNull() ?: Double.MAX_VALUE }
                .take(limit.coerceIn(1, MAX_TOOL_ITEMS))

            AgentProductListingsResult(
                productId = productId,
                totalResults = result.data.size,
                listings = listings.mapIndexed { index, listing ->
                    val analysis = if (index < MAX_ANALYZED_LISTINGS) {
                        listing.loadAnalysis()
                    } else {
                        null
                    }
                    AgentListingDealSummary(
                        listing = listing.toSummary(),
                        analysis = analysis,
                    )
                },
            )
        }
        is UiState.Error -> AgentProductListingsResult(
            productId = productId,
            totalResults = 0,
            listings = emptyList(),
            error = result.message,
        )
        else -> AgentProductListingsResult(
            productId = productId,
            totalResults = 0,
            listings = emptyList(),
            error = "Product listings not ready",
        )
    }).recordToolResult("inspectProductDeals")

    @Tool(
        description = "Analyze whether a specific platform listing price is historically good. Use when the user provides or selects a platform_product_id."
    )
    fun analyzeListingPrice(
        @ToolParam(description = "Platform product listing UUID.") platformProductId: String,
        @ToolParam(description = "Current listing price as a number.") currentPrice: Double,
        @ToolParam(description = "Original listing price as a number. Use currentPrice if original price is unavailable.") originalPrice: Double,
    ): String = (when (val result = runBlocking(Dispatchers.IO) {
        priceRecordRepository.getAnalysis(platformProductId, currentPrice, originalPrice)
    }) {
        is UiState.Success -> AgentPriceAnalysisResult(
            platformProductId = platformProductId,
            analysis = result.data.toSummary(),
        )
        is UiState.Error -> AgentPriceAnalysisResult(
            platformProductId = platformProductId,
            error = result.message,
        )
        else -> AgentPriceAnalysisResult(
            platformProductId = platformProductId,
            error = "Price analysis not ready",
        )
    }).recordToolResult("analyzeListingPrice")

    private fun PlatformListingDto.loadAnalysis(): AgentPriceAnalysisSummary? {
        val current = currentPrice.toDoubleOrNull() ?: return null
        val original = originalPrice?.toDoubleOrNull() ?: current

        return when (val result = runBlocking(Dispatchers.IO) {
            priceRecordRepository.getAnalysis(id, current, original)
        }) {
            is UiState.Success -> result.data.toSummary()
            else -> null
        }
    }

    private fun <T : Any> T.recordToolResult(name: String): String {
        val payload = gson.toJson(this)
        AgentToolResultStore.recordPayload(name, payload)
        return payload
    }
}

data class AgentProductSummary(
    val id: String?,
    val normalizedName: String?,
    val productName: String?,
    val brand: String?,
    val category: String?,
    val slug: String?,
    val mainImageUrl: String?,
)

data class AgentSearchResult(
    val keyword: String,
    val totalResults: Int,
    val products: List<AgentProductSummary>,
    val error: String? = null,
)

data class AgentCompareItemSummary(
    val id: String,
    val slug: String?,
    val productName: String,
    val lowestPrice: Double?,
    val platformCount: Int,
    val bestPlatform: String?,
    val bestListingUrl: String?,
    val estimatedSavings: Double?,
    val mainImageUrl: String?,
    val platforms: List<AgentPlatformListingSummary>,
)

data class AgentCompareResult(
    val keyword: String,
    val totalResults: Int,
    val items: List<AgentCompareItemSummary>,
    val error: String? = null,
)

data class AgentTrendingItemSummary(
    val id: String,
    val productName: String,
    val currentPrice: Double,
    val originalPrice: Double?,
    val discountPercent: Double?,
    val platformId: Int?,
    val platformName: String?,
    val url: String?,
    val inStock: Boolean,
    val mainImageUrl: String?,
)

data class AgentTrendingResult(
    val totalResults: Int,
    val items: List<AgentTrendingItemSummary>,
    val error: String? = null,
)

data class AgentPlatformListingSummary(
    val id: String,
    val productId: String,
    val platformId: Int,
    val platformName: String,
    val rawName: String?,
    val currentPrice: Double?,
    val originalPrice: Double?,
    val discountPercent: Double?,
    val inStock: Boolean,
    val url: String,
    val affiliateUrl: String?,
    val lastCrawledAt: String?,
)

data class AgentProductListingsResult(
    val productId: String,
    val totalResults: Int,
    val listings: List<AgentListingDealSummary>,
    val error: String? = null,
)

data class AgentListingDealSummary(
    val listing: AgentPlatformListingSummary,
    val analysis: AgentPriceAnalysisSummary?,
)

data class AgentPriceAnalysisResult(
    val platformProductId: String,
    val analysis: AgentPriceAnalysisSummary? = null,
    val error: String? = null,
)

data class AgentPriceAnalysisSummary(
    val currentPrice: Double,
    val lowestEverPrice: Double?,
    val averagePrice30d: Double?,
    val status: String?,
    val label: String?,
)

private fun android.app.producthunt.data.remote.dto.ProductResponse.toSummary(): AgentProductSummary =
    AgentProductSummary(
        id = id,
        normalizedName = normalizedName,
        productName = productName,
        brand = brand,
        category = category,
        slug = slug,
        mainImageUrl = mainImageUrl,
    )

private fun android.app.producthunt.data.remote.dto.SearchCompareItem.toSummary(): AgentCompareItemSummary =
    AgentCompareItemSummary(
        id = id,
        slug = slug,
        productName = productName,
        lowestPrice = lowestPrice,
        platformCount = platforms.size,
        bestPlatform = platforms
            .minByOrNull { it.currentPrice.toDoubleOrNull() ?: Double.MAX_VALUE }
            ?.platformId
            ?.platformName(),
        bestListingUrl = platforms
            .minByOrNull { it.currentPrice.toDoubleOrNull() ?: Double.MAX_VALUE }
            ?.url,
        estimatedSavings = platforms
            .mapNotNull { it.currentPrice.toDoubleOrNull() }
            .takeIf { it.size >= 2 }
            ?.let { prices -> (prices.maxOrNull() ?: 0.0) - (prices.minOrNull() ?: 0.0) },
        mainImageUrl = mainImageUrl,
        platforms = platforms
            .sortedBy { it.currentPrice.toDoubleOrNull() ?: Double.MAX_VALUE }
            .map { it.toSummary() },
    )

private fun android.app.producthunt.data.remote.dto.TrendingDealResponse.toSummary(): AgentTrendingItemSummary =
    AgentTrendingItemSummary(
        id = id,
        productName = productName,
        currentPrice = currentPrice,
        originalPrice = originalPrice,
        discountPercent = discountPercent,
        platformId = platformId,
        platformName = platformId?.platformName(),
        url = url,
        inStock = inStock,
        mainImageUrl = mainImageUrl,
    )

private fun PlatformListingDto.toSummary(): AgentPlatformListingSummary {
    val current = currentPrice.toDoubleOrNull()
    val original = originalPrice?.toDoubleOrNull()

    return AgentPlatformListingSummary(
        id = id,
        productId = productId,
        platformId = platformId,
        platformName = platformId.platformName(),
        rawName = rawName,
        currentPrice = current,
        originalPrice = original,
        discountPercent = calculateDiscountPercent(current, original),
        inStock = inStock,
        url = url,
        affiliateUrl = affiliateUrl,
        lastCrawledAt = lastCrawlledAt,
    )
}

private fun PriceAnalysisResponse.toSummary(): AgentPriceAnalysisSummary =
    AgentPriceAnalysisSummary(
        currentPrice = currentPrice,
        lowestEverPrice = allTimeLow,
        averagePrice30d = averagePrice,
        status = status,
        label = dealLabel,
    )

private fun calculateDiscountPercent(current: Double?, original: Double?): Double? {
    if (current == null || original == null || original <= 0.0 || current >= original) return null
    return ((original - current) / original) * 100.0
}

private fun Int.platformName(): String =
    when (this) {
        1 -> "Shopee"
        2 -> "Lazada"
        3 -> "Tiki"
        7 -> "FPT Shop"
        8 -> "Phong Vu"
        9 -> "CellphoneS"
        else -> "Platform $this"
    }
