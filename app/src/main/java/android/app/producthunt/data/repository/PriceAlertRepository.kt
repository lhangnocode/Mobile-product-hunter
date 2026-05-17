package android.app.producthunt.data.repository

import android.app.producthunt.data.remote.api.PriceAlertApiService
import android.app.producthunt.data.remote.dto.PriceAlertCreate
import android.app.producthunt.data.remote.dto.PriceAlertResponse
import android.app.producthunt.domain.UiState
import javax.inject.Inject

class PriceAlertRepository @Inject constructor(
    private val api: PriceAlertApiService,
) {
    suspend fun list(): UiState<List<PriceAlertResponse>> = try {
        UiState.Success(api.list())
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to load alerts")
    }

    suspend fun create(productId: String, targetPrice: Double): UiState<PriceAlertResponse> = try {
        UiState.Success(api.create(PriceAlertCreate(productId, targetPrice)))
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to create alert")
    }

    suspend fun delete(productId: String): UiState<Unit> = try {
        api.delete(productId)
        UiState.Success(Unit)
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to delete alert")
    }
}
