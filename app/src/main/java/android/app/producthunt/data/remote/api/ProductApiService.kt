package android.app.producthunt.data.remote.api

import android.app.producthunt.data.remote.dto.ProductResponse
import android.app.producthunt.data.remote.dto.SearchCompareResponse
import android.app.producthunt.data.remote.dto.SearchPaginatedResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductApiService {

    @GET("api/v1/products/")
    suspend fun getProducts(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100,
    ): List<ProductResponse>

    @GET("api/v1/products/search")
    suspend fun search(
        @Query("q") q: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): SearchPaginatedResponse

    @GET("api/v1/products/compare")
    suspend fun compare(@Query("q") q: String): SearchCompareResponse
}
