package android.app.producthunt.ui.viewmodel

import android.app.producthunt.data.remote.dto.TrendingDealResponse
import android.app.producthunt.data.repository.PlatformProductRepository
import android.app.producthunt.core.state.UiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrendingViewModel @Inject constructor(
    private val repository: PlatformProductRepository,
) : ViewModel() {

    private val _trendingState = MutableStateFlow<UiState<List<TrendingDealResponse>>>(UiState.Idle)
    val trendingState: StateFlow<UiState<List<TrendingDealResponse>>> = _trendingState.asStateFlow()

    init {
        loadTrending()
    }

    fun loadTrending(limit: Int = 20) {
        viewModelScope.launch {
            _trendingState.value = UiState.Loading
            _trendingState.value = repository.getTrending(limit)
        }
    }
}
