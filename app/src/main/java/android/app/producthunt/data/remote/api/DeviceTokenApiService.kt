package android.app.producthunt.data.remote.api

import android.app.producthunt.data.remote.dto.DeviceTokenRequest
import android.app.producthunt.data.remote.dto.DeviceTokenResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.Path

interface DeviceTokenApiService {

    @POST("api/v1/device_tokens/")
    suspend fun register(@Body body: DeviceTokenRequest): DeviceTokenResponse

    @DELETE("api/v1/device_tokens/{token}")
    suspend fun delete(@Path("token") token: String)
}
