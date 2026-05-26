package android.app.producthunt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProductResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("normalized_name") val normalizedName: String?,
    @SerializedName("product_name") val productName: String?,
    @SerializedName("raw_name") val rawName: String? = null,
    @SerializedName("slug") val slug: String?,
    @SerializedName("main_image_url") val mainImageUrl: String?,
    @SerializedName("brand") val brand: String?,
    @SerializedName("category") val category: String?,
)

data class PlatformListingDto(
    @SerializedName("id") val id: String,
    @SerializedName("product_id") val productId: String,
    @SerializedName("platform_id") val platformId: Int,
    @SerializedName("raw_name") val rawName: String?,
    @SerializedName("url") val url: String,
    @SerializedName("affiliate_url") val affiliateUrl: String?,
    @SerializedName("current_price") val currentPrice: String, 
    @SerializedName("original_price") val originalPrice: String?,
    @SerializedName("in_stock") val inStock: Boolean,
    @SerializedName("last_crawled_at") val lastCrawlledAt: String?,
)

data class SearchCompareItem(
    @SerializedName("id") val id: String,
    @SerializedName("normalized_name") val normalizedName: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("slug") val slug: String?,
    @SerializedName("main_image_url") val mainImageUrl: String?,
    @SerializedName("lowest_price") val lowestPrice: Double?,
    @SerializedName("platforms") val platforms: List<PlatformListingDto>,
)

data class SearchPaginatedResponse(
    @SerializedName("keyword") val keyword: String,
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("total_pages") val totalPages: Int,
    @SerializedName("total_results") val totalResults: Int,
    @SerializedName("data") val data: List<ProductResponse>,
)

data class SearchCompareResponse(
    @SerializedName("keyword") val keyword: String,
    @SerializedName("total_results") val totalResults: Int,
    @SerializedName("data") val data: List<SearchCompareItem>,
)

data class TrendingDealResponse(
    @SerializedName("id") val id: String,
    @SerializedName("product_name") val productName: String,
    @SerializedName("main_image_url") val mainImageUrl: String?,
    @SerializedName("current_price") val currentPrice: Double,
    @SerializedName("original_price") val originalPrice: Double?,
    @SerializedName("discount_percent") val discountPercent: Double?,
    @SerializedName("platform_id") val platformId: Int?,
    @SerializedName("url") val url: String?,
    @SerializedName("in_stock") val inStock: Boolean,
)
