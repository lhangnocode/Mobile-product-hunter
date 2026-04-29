package android.app.producthunt.data.repository

import android.app.producthunt.data.remote.api.WishlistApiService
import android.app.producthunt.data.remote.dto.WishListCreate
import android.app.producthunt.data.remote.dto.WishlistResponse
import android.app.producthunt.domain.UiState
import javax.inject.Inject

class WishlistRepository @Inject constructor(
    private val api: WishlistApiService,
) {
    suspend fun get(): UiState<List<WishlistResponse>> = try {
        UiState.Success(api.get())
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to load wishlist")
    }

    suspend fun add(productId: String): UiState<WishlistResponse> = try {
        UiState.Success(api.add(WishListCreate(productId)))
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to add to wishlist")
    }

    suspend fun remove(productId: String): UiState<Unit> = try {
        api.remove(productId)
        UiState.Success(Unit)
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to remove from wishlist")
    }
}
