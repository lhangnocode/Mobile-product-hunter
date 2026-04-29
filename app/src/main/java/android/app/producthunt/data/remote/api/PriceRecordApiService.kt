package android.app.producthunt.data.remote.api

import android.app.producthunt.data.remote.dto.PriceAnalysisResponse
import android.app.producthunt.data.remote.dto.PriceRecordResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PriceRecordApiService {

    @GET("api/v1/price_record/price-records/{platform_product_id}")
    suspend fun getHistory(
        @Path("platform_product_id") platformProductId: String,
    ): List<PriceRecordResponse>

    @GET("api/v1/price_record/price-analysis/{platform_product_id}")
    suspend fun getAnalysis(
        @Path("platform_product_id") platformProductId: String,
        @Query("current_price") currentPrice: Double,
        @Query("original_price") originalPrice: Double,
    ): PriceAnalysisResponse
}
