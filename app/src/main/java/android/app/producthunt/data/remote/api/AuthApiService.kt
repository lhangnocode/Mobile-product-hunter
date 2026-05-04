package android.app.producthunt.data.remote.api

import android.app.producthunt.data.remote.dto.RefreshTokenRequest
import android.app.producthunt.data.remote.dto.RegisterRequest
import android.app.producthunt.data.remote.dto.TokenResponse
import android.app.producthunt.data.remote.dto.UserResponse
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    @POST("api/v1/auth/register")
    suspend fun register(@Body body: RegisterRequest): UserResponse

    @FormUrlEncoded
    @POST("api/v1/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
    ): TokenResponse

    @GET("api/v1/auth/me")
    suspend fun me(): UserResponse

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body body: RefreshTokenRequest): TokenResponse
}
