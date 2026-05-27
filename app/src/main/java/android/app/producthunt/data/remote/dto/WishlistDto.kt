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
    
    // Các trường fallback nếu API trả về cấu trúc phẳng (không lồng trong product)
    @SerializedName("product_name") val productName: String? = null,
    @SerializedName("raw_name") val rawName: String? = null,
    @SerializedName("normalized_name") val normalizedName: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("title") val title: String? = null,
    
    @SerializedName("main_image_url") val mainImageUrl: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("image") val image: String? = null,

    @SerializedName("brand") val brand: String? = null,
    
    // Các trường giá và giảm giá nếu có
    @SerializedName("current_price") val currentPrice: Double? = null,
    @SerializedName("original_price") val originalPrice: Double? = null,
    @SerializedName("discount_percent") val discountPercent: Double? = null,
    @SerializedName("lowest_price") val lowestPrice: Double? = null,
)
