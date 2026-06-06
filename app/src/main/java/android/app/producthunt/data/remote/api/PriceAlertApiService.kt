package android.app.producthunt.data.remote.api

import android.app.producthunt.data.remote.dto.PriceAlertCreate
import android.app.producthunt.data.remote.dto.PriceAlertResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PriceAlertApiService {

    @POST("api/v1/price_alerts/")
    suspend fun create(@Body body: PriceAlertCreate): PriceAlertResponse

    @GET("api/v1/price_alerts/")
    suspend fun list(): List<PriceAlertResponse>

    @DELETE("api/v1/price_alerts/{platform_product_id}")
    suspend fun delete(@Path("platform_product_id") platformProductId: String)
}
