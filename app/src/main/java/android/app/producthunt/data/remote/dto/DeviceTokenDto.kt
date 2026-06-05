package android.app.producthunt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DeviceTokenRequest(
    @SerializedName("token") val token: String,
    @SerializedName("platform") val platform: String = "android",
)
