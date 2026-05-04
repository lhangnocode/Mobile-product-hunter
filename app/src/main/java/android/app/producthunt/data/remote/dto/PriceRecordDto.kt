package android.app.producthunt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PriceRecordResponse(
    @SerializedName("id") val id: String,
    @SerializedName("platform_product_id") val platformProductId: String,
    @SerializedName("price") val price: Double,
    @SerializedName("original_price") val originalPrice: Double?,
    @SerializedName("is_flash_sale") val isFlashSale: Boolean,
    @SerializedName("recorded_at") val recordedAt: String,
)

data class PriceRecordCreateRequest(
    @SerializedName("platform_product_id") val platformProductId: String,
    @SerializedName("price") val price: Double,
    @SerializedName("original_price") val originalPrice: Double?,
    @SerializedName("is_flash_sale") val isFlashSale: Boolean = false,
    @SerializedName("recorded_at") val recordedAt: String? = null,
)

data class PriceAnalysisResponse(
    @SerializedName("platform_product_id") val platformProductId: String,
    @SerializedName("current_price") val currentPrice: Double,
    @SerializedName("original_price") val originalPrice: Double?,
    @SerializedName("all_time_low") val allTimeLow: Double?,
    @SerializedName("all_time_high") val allTimeHigh: Double?,
    @SerializedName("average_price") val averagePrice: Double?,
    @SerializedName("status") val status: String?,
    @SerializedName("discount_from_original") val discountFromOriginal: Double?,
)
