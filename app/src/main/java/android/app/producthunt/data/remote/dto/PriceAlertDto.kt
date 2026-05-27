package android.app.producthunt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PriceAlertCreate(
    @SerializedName("product_id") val productId: String,
    @SerializedName("target_price") val targetPrice: Double,
)

data class PriceAlertResponse(
    @SerializedName("id") val id: String,
    @SerializedName("product_id") val productId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("target_price") val targetPrice: Double,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("product") val product: ProductResponse?,
    // Fallback fields for display
    @SerializedName("product_name") val productName: String? = null,
    @SerializedName("main_image_url") val mainImageUrl: String? = null,
)
