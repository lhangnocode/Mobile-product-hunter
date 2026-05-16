package android.app.producthunt.ui.viewmodel

import android.app.producthunt.data.remote.dto.*
import android.app.producthunt.data.repository.*
import android.app.producthunt.domain.UiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val platformProductRepository: PlatformProductRepository,
    private val priceRecordRepository: PriceRecordRepository,
    private val wishlistRepository: WishlistRepository,
) : ViewModel() {

    private val _listingsState = MutableStateFlow<UiState<List<PlatformListingDto>>>(UiState.Idle)
    val listingsState: StateFlow<UiState<List<PlatformListingDto>>> = _listingsState.asStateFlow()

    private val _historyState = MutableStateFlow<UiState<List<PriceRecordResponse>>>(UiState.Idle)
    val historyState: StateFlow<UiState<List<PriceRecordResponse>>> = _historyState.asStateFlow()

    private val _analysisState = MutableStateFlow<UiState<PriceAnalysisResponse>>(UiState.Idle)
    val analysisState: StateFlow<UiState<PriceAnalysisResponse>> = _analysisState.asStateFlow()

    private val _wishlistActionState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val wishlistActionState: StateFlow<UiState<Unit>> = _wishlistActionState.asStateFlow()

    fun loadProductDetails(productId: String) {
        viewModelScope.launch {
            _listingsState.value = UiState.Loading
            val listingsResult = platformProductRepository.getListingsByProductId(productId)
            _listingsState.value = listingsResult

            if (listingsResult is UiState.Success && listingsResult.data.isNotEmpty()) {
                val firstListing = listingsResult.data.first()
                
                // Chuyển đổi giá từ String sang Double để gọi API Analysis
                val currentPrice = firstListing.currentPrice.toDoubleOrNull() ?: 0.0
                val originalPrice = firstListing.originalPrice?.toDoubleOrNull() ?: currentPrice
                
                loadPriceHistory(firstListing.id)
                loadAnalysis(firstListing.id, currentPrice, originalPrice)
            }
        }
    }

    private fun loadPriceHistory(platformProductId: String) {
        viewModelScope.launch {
            _historyState.value = UiState.Loading
            _historyState.value = priceRecordRepository.getHistory(platformProductId)
        }
    }

    private fun loadAnalysis(platformProductId: String, currentPrice: Double, originalPrice: Double) {
        viewModelScope.launch {
            _analysisState.value = UiState.Loading
            _analysisState.value = priceRecordRepository.getAnalysis(platformProductId, currentPrice, originalPrice)
        }
    }

    fun toggleWishlist(productId: String) {
        viewModelScope.launch {
            _wishlistActionState.value = UiState.Loading
            val result = wishlistRepository.add(productId)
            _wishlistActionState.value = when (result) {
                is UiState.Success -> UiState.Success(Unit)
                is UiState.Error -> UiState.Error(result.message)
                else -> UiState.Idle
            }
        }
    }
}
