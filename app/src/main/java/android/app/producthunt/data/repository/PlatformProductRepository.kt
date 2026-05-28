package android.app.producthunt.data.repository

import android.app.producthunt.data.remote.api.PlatformProductApiService
import android.app.producthunt.data.remote.dto.PlatformListingDto
import android.app.producthunt.data.remote.dto.TrendingDealResponse
import android.app.producthunt.core.state.UiState
import javax.inject.Inject

class PlatformProductRepository @Inject constructor(
    private val api: PlatformProductApiService,
) {
    suspend fun getTrending(limit: Int = 20): UiState<List<TrendingDealResponse>> = try {
        UiState.Success(api.getTrending(limit))
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to load trending deals")
    }

    suspend fun getListingsByProductId(productId: String): UiState<List<PlatformListingDto>> = try {
        UiState.Success(api.getListingsByProductId(productId))
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to load product listings")
    }
}
