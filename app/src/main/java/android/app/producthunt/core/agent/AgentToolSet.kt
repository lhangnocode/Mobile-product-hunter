package android.app.producthunt.core.agent

import android.app.producthunt.core.state.UiState
import android.app.producthunt.data.remote.dto.PlatformListingDto
import android.app.producthunt.data.remote.dto.PriceAnalysisResponse
import android.app.producthunt.data.remote.dto.PriceRecordResponse
import android.app.producthunt.data.repository.PlatformProductRepository
import android.app.producthunt.data.repository.PriceRecordRepository
import android.app.producthunt.data.repository.ProductRepository
import com.google.ai.edge.litertlm.Tool
import com.google.ai.edge.litertlm.ToolParam
import com.google.ai.edge.litertlm.ToolSet
import com.google.gson.Gson
import com.google.gson.JsonElement
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
    }

    @Tool(
        description = "Search canonical products by keyword. Use this for broad discovery, categories, budgets, and unclear product names before recommending products."
    )
    fun searchProducts(
        @ToolParam(description = "Product keyword, category, brand, or user phrase. Minimum 2 characters.") query: String,
        @ToolParam(description = "Page number (1-based).") page: Int = 1,
        @ToolParam(description = "Number of products to return. Keep small for concise answers.") limit: Int = MAX_TOOL_ITEMS,
    ): JsonElement {
        AgentToolResultStore.recordStarted(
            name = "searchProducts",
            input = "query=$query, page=$page, limit=${limit.coerceIn(1, MAX_TOOL_ITEMS)}",
        )
        return (when (val result = runBlocking(Dispatchers.IO) {
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
    }

    @Tool(
        description = "Get current trending deals from /api/v1/platform_products/platform-products/trending. Use for hot deals, today's deals, discounts, and bargain hunting."
    )
    fun getTrendingDeals(
        @ToolParam(description = "Maximum number of deals to return.") limit: Int = MAX_TOOL_ITEMS,
    ): JsonElement {
        AgentToolResultStore.recordStarted(
            name = "getTrendingDeals",
            input = "limit=${limit.coerceIn(1, MAX_TOOL_ITEMS)}",
        )
        return (when (val result = runBlocking(Dispatchers.IO) {
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
    }

    @Tool(
        description = "Get product detail for a canonical product id using /api/v1/platform_products/platform-products/by-product-id. Returns marketplace listings, prices, stock, platform names, and URLs."
    )
    fun getProductDetail(
        @ToolParam(description = "Canonical product UUID returned by searchProducts.") productId: String,
        @ToolParam(description = "Maximum number of marketplace listings to return.") limit: Int = MAX_TOOL_ITEMS,
    ): JsonElement {
        AgentToolResultStore.recordStarted(
            name = "getProductDetail",
            input = "productId=$productId, limit=${limit.coerceIn(1, MAX_TOOL_ITEMS)}",
        )
        return (when (val result = runBlocking(Dispatchers.IO) {
            platformProductRepository.getListingsByProductId(productId)
        }) {
        is UiState.Success -> {
            val listings = result.data
                .sortedBy { it.currentPrice.toDoubleOrNull() ?: Double.MAX_VALUE }
                .take(limit.coerceIn(1, MAX_TOOL_ITEMS))

            AgentProductListingsResult(
                productId = productId,
                totalResults = result.data.size,
                listings = listings.map { it.toSummary() },
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
        }).recordToolResult("getProductDetail")
    }

    @Tool(
        description = "Get price history records for a platform product listing from /api/v1/price_record/price-records/{platform_product_id}. Prefer this for price trend, history, lowest price, and deal-quality questions."
    )
    fun getProductPriceRecords(
        @ToolParam(description = "Platform product listing UUID from getProductDetail listings.") platformProductId: String,
        @ToolParam(description = "Maximum number of recent price records to return.") limit: Int = MAX_TOOL_ITEMS,
    ): JsonElement {
        AgentToolResultStore.recordStarted(
            name = "getProductPriceRecords",
            input = "platformProductId=$platformProductId, limit=${limit.coerceIn(1, MAX_TOOL_ITEMS)}",
        )
        return (when (val result = runBlocking(Dispatchers.IO) {
            priceRecordRepository.getHistory(platformProductId)
        }) {
        is UiState.Success -> {
            val records = result.data
            AgentPriceRecordsResult(
                platformProductId = platformProductId,
                totalRecords = records.size,
                latest = records.lastOrNull()?.toSummary(),
                lowestPrice = records.mapNotNull { it.price.toDoubleOrNull() }.minOrNull(),
                highestPrice = records.mapNotNull { it.price.toDoubleOrNull() }.maxOrNull(),
                recentRecords = records
                    .takeLast(limit.coerceIn(1, MAX_TOOL_ITEMS))
                    .asReversed()
                    .map { it.toSummary() },
            )
        }
        is UiState.Error -> AgentPriceRecordsResult(
            platformProductId = platformProductId,
            totalRecords = 0,
            latest = null,
            lowestPrice = null,
            highestPrice = null,
            recentRecords = emptyList(),
            error = result.message,
        )
        else -> AgentPriceRecordsResult(
            platformProductId = platformProductId,
            totalRecords = 0,
            latest = null,
            lowestPrice = null,
            highestPrice = null,
            recentRecords = emptyList(),
            error = "Price records not ready",
        )
        }).recordToolResult("getProductPriceRecords")
    }

    @Tool(
        description = "Analyze whether a specific platform listing price is historically good. Use only when a direct status label is needed; prefer getProductPriceRecords for price history and trend evidence."
    )
    fun analyzeListingPrice(
        @ToolParam(description = "Platform product listing UUID.") platformProductId: String,
        @ToolParam(description = "Current listing price as a number.") currentPrice: Double,
        @ToolParam(description = "Original listing price as a number. Use currentPrice if original price is unavailable.") originalPrice: Double,
    ): JsonElement {
        AgentToolResultStore.recordStarted(
            name = "analyzeListingPrice",
            input = "platformProductId=$platformProductId, currentPrice=$currentPrice, originalPrice=$originalPrice",
        )
        return (when (val result = runBlocking(Dispatchers.IO) {
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
    }

    private fun <T : Any> T.recordToolResult(name: String): JsonElement {
        val payload = gson.toJson(this)
        AgentToolResultStore.recordPayload(name, payload)
        return gson.toJsonTree(this)
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
    val listings: List<AgentPlatformListingSummary>,
    val error: String? = null,
)

data class AgentPriceAnalysisResult(
    val platformProductId: String,
    val analysis: AgentPriceAnalysisSummary? = null,
    val error: String? = null,
)

data class AgentPriceRecordsResult(
    val platformProductId: String,
    val totalRecords: Int,
    val latest: AgentPriceRecordSummary?,
    val lowestPrice: Double?,
    val highestPrice: Double?,
    val recentRecords: List<AgentPriceRecordSummary>,
    val error: String? = null,
)

data class AgentPriceRecordSummary(
    val id: Int,
    val price: Double?,
    val originalPrice: Double?,
    val isFlashSale: Boolean,
    val recordedAt: String,
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

private fun PriceRecordResponse.toSummary(): AgentPriceRecordSummary =
    AgentPriceRecordSummary(
        id = id,
        price = price.toDoubleOrNull(),
        originalPrice = originalPrice?.toDoubleOrNull(),
        isFlashSale = isFlashSale,
        recordedAt = recordedAt,
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
