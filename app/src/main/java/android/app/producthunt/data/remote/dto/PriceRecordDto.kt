package android.app.producthunt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PriceRecordResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("platform_product_id") val platformProductId: String,
    @SerializedName("price") val price: String, // Server trả về String
    @SerializedName("original_price") val originalPrice: String?,
    @SerializedName("is_flash_sale") val isFlashSale: Boolean,
    @SerializedName("recorded_at") val recordedAt: String,
)

data class PriceAnalysisResponse(
    @SerializedName("current_price") val currentPrice: Double,
    @SerializedName("lowest_ever_price") val allTimeLow: Double?,
    @SerializedName("avg_price_30d") val averagePrice: Double?,
    @SerializedName("deal_status") val status: String?,
    @SerializedName("deal_label") val dealLabel: String?,
)
