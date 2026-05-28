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
    @SerializedName("current_price") val currentPrice: Double? = null,
    @SerializedName("last_checked_price") val lastCheckedPrice: Double? = null,
    @SerializedName("latest_price") val latestPrice: Double? = null,
    @SerializedName("price") val price: Double? = null,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("status") val status: String? = null,
    @SerializedName("target_reached") val targetReached: Boolean? = null,
    @SerializedName("is_target_reached") val isTargetReached: Boolean? = null,
    @SerializedName("triggered") val triggered: Boolean? = null,
    @SerializedName("is_triggered") val isTriggered: Boolean? = null,
    @SerializedName("triggered_at") val triggeredAt: String? = null,
    @SerializedName("last_triggered_at") val lastTriggeredAt: String? = null,
    @SerializedName("notified_at") val notifiedAt: String? = null,
    @SerializedName("last_notified_at") val lastNotifiedAt: String? = null,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("product") val product: ProductResponse?,
    // Fallback fields for display
    @SerializedName("product_name") val productName: String? = null,
    @SerializedName("main_image_url") val mainImageUrl: String? = null,
)

data class TriggerAlertRequest(
    @SerializedName("product_id") val productId: String? = null,
)

data class TriggerAlertResponse(
    @SerializedName("status") val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("triggered_count") val triggeredCount: Int? = null,
    @SerializedName("sent_count") val sentCount: Int? = null,
    @SerializedName("matched_count") val matchedCount: Int? = null,
)
