package android.app.producthunt.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WishListCreate(
    @SerializedName("product_id") val productId: String,
)

data class WishlistResponse(
    @SerializedName("id") val id: String,
    @SerializedName("product_id") val productId: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("created_at") val createdAt: String?,
    @SerializedName("product") val product: ProductResponse?,
)
