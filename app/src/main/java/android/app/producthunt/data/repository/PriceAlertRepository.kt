package android.app.producthunt.data.repository

import android.app.producthunt.data.remote.ApiErrorParser
import android.app.producthunt.data.remote.api.PriceAlertApiService
import android.app.producthunt.data.remote.dto.PriceAlertCreate
import android.app.producthunt.data.remote.dto.PriceAlertResponse
import android.app.producthunt.data.remote.dto.TriggerAlertRequest
import android.app.producthunt.data.remote.dto.TriggerAlertResponse
import android.app.producthunt.domain.UiState
import kotlinx.coroutines.delay
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

        _alerts.value = try {
            UiState.Success(api.list())
        } catch (e: Exception) {
            UiState.Error(ApiErrorParser.messageFrom(e, "Failed to load alerts"))
        }
    }

    suspend fun create(productId: String, targetPrice: Double): UiState<PriceAlertResponse> = try {
        val response = api.create(PriceAlertCreate(productId, targetPrice))
        refresh()
        UiState.Success(response)
    } catch (e: Exception) {
        UiState.Error(ApiErrorParser.messageFrom(e, "Failed to create alert"))
    }

    suspend fun delete(productId: String): UiState<Unit> = try {
        api.delete(productId)
        refresh()
        UiState.Success(Unit)
    } catch (e: Exception) {
        UiState.Error(ApiErrorParser.messageFrom(e, "Failed to delete alert"))
    }

    suspend fun trigger(productId: String? = null): UiState<TriggerAlertResponse> = try {
        val response = api.trigger(TriggerAlertRequest(productId))
        refresh()
        delay(750)
        refresh()
        UiState.Success(response)
    } catch (e: Exception) {
        UiState.Error(ApiErrorParser.messageFrom(e, "Failed to trigger price check"))
    }

    fun clear() {
        _alerts.value = UiState.Idle
    }
}
