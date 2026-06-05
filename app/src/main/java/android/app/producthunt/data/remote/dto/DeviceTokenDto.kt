package android.app.producthunt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeviceTokenRequest(
    @SerializedName("token") val token: String,
    @SerializedName("platform") val platform: String = "android",
)

data class DeviceTokenResponse(
    @SerializedName("id") val id: String,
    @SerializedName("token") val token: String,
    @SerializedName("platform") val platform: String,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("last_seen_at") val lastSeenAt: String? = null,
)
