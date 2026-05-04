package android.app.producthunt.data.remote.api

import android.app.producthunt.data.remote.dto.TrendingDealResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PlatformProductApiService {

    @GET("api/v1/platform_products/platform-products/trending")
    suspend fun getTrending(@Query("limit") limit: Int = 20): List<TrendingDealResponse>
}
