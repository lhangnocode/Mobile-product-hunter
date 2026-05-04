package android.app.producthunt.ui.viewmodel

import android.app.producthunt.data.remote.dto.PriceAnalysisResponse
import android.app.producthunt.data.remote.dto.PriceRecordResponse
import android.app.producthunt.data.repository.PriceRecordRepository
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
class ProductDetailViewModel @Inject constructor(
    private val repository: PriceRecordRepository,
) : ViewModel() {

    private val _historyState = MutableStateFlow<UiState<List<PriceRecordResponse>>>(UiState.Idle)
    val historyState: StateFlow<UiState<List<PriceRecordResponse>>> = _historyState.asStateFlow()

    private val _analysisState = MutableStateFlow<UiState<PriceAnalysisResponse>>(UiState.Idle)
    val analysisState: StateFlow<UiState<PriceAnalysisResponse>> = _analysisState.asStateFlow()

    fun loadPriceHistory(platformProductId: String) {
        viewModelScope.launch {
            _historyState.value = UiState.Loading
            _historyState.value = repository.getHistory(platformProductId)
        }
    }

    fun loadAnalysis(platformProductId: String, currentPrice: Double, originalPrice: Double) {
        viewModelScope.launch {
            _analysisState.value = UiState.Loading
            _analysisState.value = repository.getAnalysis(platformProductId, currentPrice, originalPrice)
        }
    }
}
