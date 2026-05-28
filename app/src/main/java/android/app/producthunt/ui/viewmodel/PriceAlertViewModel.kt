package android.app.producthunt.ui.viewmodel

import android.app.producthunt.data.remote.dto.PriceAlertResponse
import android.app.producthunt.data.remote.dto.TriggerAlertResponse
import android.app.producthunt.data.repository.PriceAlertRepository
import android.app.producthunt.core.state.UiState
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
) : ViewModel() {

    val alertsState: StateFlow<UiState<List<PriceAlertResponse>>> = repository.alerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Idle)

    private val _createState = MutableStateFlow<UiState<PriceAlertResponse>>(UiState.Idle)
    val createState: StateFlow<UiState<PriceAlertResponse>> = _createState.asStateFlow()

    private val _triggerState = MutableStateFlow<UiState<TriggerAlertResponse>>(UiState.Idle)
    val triggerState: StateFlow<UiState<TriggerAlertResponse>> = _triggerState.asStateFlow()

    private val _deleteAllState = MutableStateFlow<UiState<Int>>(UiState.Idle)
    val deleteAllState: StateFlow<UiState<Int>> = _deleteAllState.asStateFlow()

    init {
        loadAlerts()
    }

    fun loadAlerts() {
        viewModelScope.launch {
            repository.refresh()
        }
    }

    fun create(productId: String, targetPrice: Double) {
        viewModelScope.launch {
            _createState.value = UiState.Loading
            val result = repository.create(productId, targetPrice)
            _createState.value = result
        }
    }

    fun delete(productId: String) {
        viewModelScope.launch {
            repository.delete(productId)
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
                when (repository.delete(alert.productId)) {
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

    fun trigger(productId: String? = null) {
        viewModelScope.launch {
            _triggerState.value = UiState.Loading
            val result = repository.trigger(productId)
            _triggerState.value = result
        }
    }

    fun resetCreateState() {
        _createState.value = UiState.Idle
    }

    fun resetTriggerState() {
        _triggerState.value = UiState.Idle
    }

    fun resetDeleteAllState() {
        _deleteAllState.value = UiState.Idle
    }
}
