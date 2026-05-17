package android.app.producthunt.data.remote.api

import android.app.producthunt.data.remote.dto.WishListCreate
import android.app.producthunt.data.remote.dto.WishlistResponse
import com.google.gson.JsonElement
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface WishlistApiService {

    @POST("api/v1/wish_lists/")
    suspend fun add(@Body body: WishListCreate): WishlistResponse

    @GET("api/v1/wish_lists/")
    suspend fun get(): JsonElement

    @DELETE("api/v1/wish_lists/{product_id}")
    suspend fun remove(@Path("product_id") productId: String)
}
