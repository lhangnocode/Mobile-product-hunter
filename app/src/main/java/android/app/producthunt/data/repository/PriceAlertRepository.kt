package android.app.producthunt.data.repository

import android.app.producthunt.data.remote.api.PriceAlertApiService
import android.app.producthunt.data.remote.dto.PriceAlertCreate
import android.app.producthunt.data.remote.dto.PriceAlertResponse
import android.app.producthunt.data.remote.dto.TriggerAlertResponse
import android.app.producthunt.data.remote.dto.TriggerAlertRequest
import android.app.producthunt.core.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class PriceAlertRepository @Inject constructor(
    private val api: PriceAlertApiService,
) {
    private val _alerts = MutableStateFlow<UiState<List<PriceAlertResponse>>>(UiState.Idle)
    val alerts: StateFlow<UiState<List<PriceAlertResponse>>> = _alerts.asStateFlow()

    suspend fun refresh() {
        if (_alerts.value !is UiState.Success) {
            _alerts.value = UiState.Loading
        }
        try {
            _alerts.value = UiState.Success(api.list())
        } catch (e: Exception) {
            if (_alerts.value !is UiState.Success) {
                _alerts.value = UiState.Error(e.message ?: "Failed to load alerts")
            }
        }
    }

    suspend fun create(
        platformProductId: String,
        productId: String? = null,
        targetPrice: Double,
    ): UiState<PriceAlertResponse> = try {
        val result = api.create(
            PriceAlertCreate(
                platformProductId = platformProductId,
                productId = productId,
                targetPrice = targetPrice,
            ),
        )
        refresh()
        UiState.Success(result)
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to create alert")
    }

    suspend fun delete(platformProductId: String): UiState<Unit> = try {
        api.delete(platformProductId)
        refresh()
        UiState.Success(Unit)
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to delete alert")
    }

    suspend fun trigger(
        productId: String? = null,
        platformProductId: String? = null,
    ): UiState<TriggerAlertResponse> = try {
        UiState.Success(api.trigger(TriggerAlertRequest(productId, platformProductId)))
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to trigger alert")
    }

    fun clear() {
        _alerts.value = UiState.Idle
    }
}
