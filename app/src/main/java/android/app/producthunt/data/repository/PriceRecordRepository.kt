package android.app.producthunt.data.repository

import android.app.producthunt.data.remote.api.PriceRecordApiService
import android.app.producthunt.data.remote.dto.PriceAnalysisResponse
import android.app.producthunt.data.remote.dto.PriceRecordResponse
import android.app.producthunt.domain.UiState
import javax.inject.Inject

class PriceRecordRepository @Inject constructor(
    private val api: PriceRecordApiService,
) {
    suspend fun getHistory(platformProductId: String): UiState<List<PriceRecordResponse>> = try {
        UiState.Success(api.getHistory(platformProductId))
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to load price history")
    }

    suspend fun getAnalysis(
        platformProductId: String,
        currentPrice: Double,
        originalPrice: Double,
    ): UiState<PriceAnalysisResponse> = try {
        UiState.Success(api.getAnalysis(platformProductId, currentPrice, originalPrice))
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to load price analysis")
    }
}
