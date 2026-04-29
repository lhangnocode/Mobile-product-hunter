package android.app.producthunt.data.repository

import android.app.producthunt.data.remote.api.PlatformProductApiService
import android.app.producthunt.data.remote.dto.TrendingDealResponse
import android.app.producthunt.domain.UiState
import javax.inject.Inject

class PlatformProductRepository @Inject constructor(
    private val api: PlatformProductApiService,
) {
    suspend fun getTrending(limit: Int = 20): UiState<List<TrendingDealResponse>> = try {
        UiState.Success(api.getTrending(limit))
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to load trending deals")
    }
}
