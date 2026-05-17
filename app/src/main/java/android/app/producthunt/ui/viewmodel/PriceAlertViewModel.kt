package android.app.producthunt.ui.viewmodel

import android.app.producthunt.data.remote.dto.PriceAlertResponse
import android.app.producthunt.data.repository.PriceAlertRepository
import android.app.producthunt.domain.UiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PriceAlertViewModel @Inject constructor(
    private val repository: PriceAlertRepository,
) : ViewModel() {

    private val _alertsState = MutableStateFlow<UiState<List<PriceAlertResponse>>>(UiState.Idle)
    val alertsState: StateFlow<UiState<List<PriceAlertResponse>>> = _alertsState.asStateFlow()

    private val _createState = MutableStateFlow<UiState<PriceAlertResponse>>(UiState.Idle)
    val createState: StateFlow<UiState<PriceAlertResponse>> = _createState.asStateFlow()

    init {
        loadAlerts()
    }

    fun loadAlerts() {
        viewModelScope.launch {
            _alertsState.value = UiState.Loading
            _alertsState.value = repository.list()
        }
    }

    fun create(productId: String, targetPrice: Double) {
        viewModelScope.launch {
            _createState.value = UiState.Loading
            val result = repository.create(productId, targetPrice)
            _createState.value = result
            if (result is UiState.Success) loadAlerts()
        }
    }

    fun delete(productId: String) {
        viewModelScope.launch {
            repository.delete(productId)
            loadAlerts()
        }
    }

    fun resetCreateState() {
        _createState.value = UiState.Idle
    }
}
