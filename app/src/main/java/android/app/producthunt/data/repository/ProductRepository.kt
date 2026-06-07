package android.app.producthunt.data.repository

import android.app.producthunt.data.remote.api.ProductApiService
import android.app.producthunt.data.remote.dto.ProductResponse
import android.app.producthunt.data.remote.dto.SearchCompareResponse
import android.app.producthunt.data.remote.dto.SearchPaginatedResponse
import android.app.producthunt.core.state.UiState
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val api: ProductApiService,
) {
    suspend fun getProducts(skip: Int = 0, limit: Int = 100): UiState<List<ProductResponse>> = try {
        UiState.Success(api.getProducts(skip, limit))
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to load products")
    }

    suspend fun getProductById(productId: String): UiState<ProductResponse> = try {
        val product = api.getProducts(limit = PRODUCT_LOOKUP_LIMIT)
            .firstOrNull { it.id == productId }
        if (product != null) {
            UiState.Success(product)
        } else {
            UiState.Error("Product not found")
        }
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to load product")
    }

    suspend fun search(q: String, page: Int = 1, limit: Int = 20): UiState<SearchPaginatedResponse> = try {
        UiState.Success(api.search(q, page, limit))
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Search failed")
    }

    suspend fun compare(q: String): UiState<SearchCompareResponse> = try {
        UiState.Success(api.compare(q))
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Compare failed")
    }

    private companion object {
        const val PRODUCT_LOOKUP_LIMIT = 1000
    }
}
