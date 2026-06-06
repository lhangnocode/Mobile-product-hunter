package android.app.producthunt.ui.viewmodel

import android.app.producthunt.core.notification.PriceAlertRefreshEvents
import android.app.producthunt.core.state.UiState
import android.app.producthunt.data.remote.dto.PriceAlertResponse
import android.app.producthunt.data.repository.PriceAlertRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PriceAlertViewModel @Inject constructor(
    private val repository: PriceAlertRepository,
    private val priceAlertRefreshEvents: PriceAlertRefreshEvents,
) : ViewModel() {

    val alertsState: StateFlow<UiState<List<PriceAlertResponse>>> = repository.alerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Idle)

    private val _createState = MutableStateFlow<UiState<PriceAlertResponse>>(UiState.Idle)
    val createState: StateFlow<UiState<PriceAlertResponse>> = _createState.asStateFlow()

    private val _deleteAllState = MutableStateFlow<UiState<Int>>(UiState.Idle)
    val deleteAllState: StateFlow<UiState<Int>> = _deleteAllState.asStateFlow()

    private val _deleteState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val deleteState: StateFlow<UiState<Unit>> = _deleteState.asStateFlow()

    init {
        loadAlerts()
        viewModelScope.launch {
            priceAlertRefreshEvents.events.collect {
                repository.refresh()
            }
        }
    }

    fun loadAlerts() {
        viewModelScope.launch {
            repository.refresh()
        }
    }

    fun create(platformProductId: String, targetPrice: Double) {
        viewModelScope.launch {
            _createState.value = UiState.Loading
            val result = repository.create(
                platformProductId = platformProductId,
                targetPrice = targetPrice,
            )
            _createState.value = result
        }
    }

    fun delete(platformProductId: String) {
        viewModelScope.launch {
            _deleteState.value = UiState.Loading
            _deleteState.value = repository.delete(platformProductId)
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            val currentAlerts = (alertsState.value as? UiState.Success)?.data.orEmpty()
            if (currentAlerts.isEmpty()) {
                _deleteAllState.value = UiState.Success(0)
                return@launch
            }

            _deleteAllState.value = UiState.Loading
            var deletedCount = 0
            currentAlerts.forEach { alert ->
                when (repository.delete(alert.platformProductId)) {
                    is UiState.Success -> deletedCount += 1
                    is UiState.Error -> {
                        _deleteAllState.value = UiState.Error("Failed to clear all alerts")
                        return@launch
                    }
                    else -> Unit
                }
            }
            _deleteAllState.value = UiState.Success(deletedCount)
        }
    }

    fun resetCreateState() {
        _createState.value = UiState.Idle
    }

    fun resetDeleteAllState() {
        _deleteAllState.value = UiState.Idle
    }

    fun resetDeleteState() {
        _deleteState.value = UiState.Idle
    }
}
