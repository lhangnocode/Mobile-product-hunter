package android.app.producthunt.data.remote.api

import android.app.producthunt.data.remote.dto.PlatformListingDto
import android.app.producthunt.data.remote.dto.TrendingDealResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PlatformProductApiService {

    @GET("api/v1/platform_products/platform-products/trending")
    suspend fun getTrending(@Query("limit") limit: Int = 20): List<TrendingDealResponse>

    @GET("api/v1/platform_products/platform-products/by-product-id")
    suspend fun getListingsByProductId(
        @Query("product_id") productId: String,
        @Query("limit") limit: Int = 20,
        @Query("page") page: Int = 1
    ): List<PlatformListingDto>
}
