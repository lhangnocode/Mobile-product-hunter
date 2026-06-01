package android.app.producthunt.data.remote.api

import android.app.producthunt.data.remote.dto.PriceAlertCreate
import android.app.producthunt.data.remote.dto.PriceAlertResponse
import android.app.producthunt.data.remote.dto.TriggerAlertRequest
import android.app.producthunt.data.remote.dto.TriggerAlertResponse
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

    @DELETE("api/v1/price_alerts/{product_id}")
    suspend fun delete(@Path("product_id") productId: String)

    @POST("api/v1/price_alerts/trigger")
    suspend fun trigger(@Body body: TriggerAlertRequest): TriggerAlertResponse
}
