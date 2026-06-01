package android.app.producthunt.ui.viewmodel

import android.app.producthunt.data.remote.dto.*
import android.app.producthunt.data.repository.*
import android.app.producthunt.core.state.UiState
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
    private val priceAlertRepository: PriceAlertRepository,
) : ViewModel() {

    private val _listingsState = MutableStateFlow<UiState<List<PlatformListingDto>>>(UiState.Idle)
    val listingsState: StateFlow<UiState<List<PlatformListingDto>>> = _listingsState.asStateFlow()

    private val _historyState = MutableStateFlow<UiState<List<PriceRecordResponse>>>(UiState.Idle)
    val historyState: StateFlow<UiState<List<PriceRecordResponse>>> = _historyState.asStateFlow()

    private val _analysisState = MutableStateFlow<UiState<PriceAnalysisResponse>>(UiState.Idle)
    val analysisState: StateFlow<UiState<PriceAnalysisResponse>> = _analysisState.asStateFlow()

    private val _priceAlertState = MutableStateFlow<UiState<PriceAlertResponse>>(UiState.Idle)
    val priceAlertState: StateFlow<UiState<PriceAlertResponse>> = _priceAlertState.asStateFlow()

    private val currentProductId = MutableStateFlow<String?>(null)

    // Quan sát danh sách yêu thích từ Repository để đồng bộ mọi nơi
    val isWishlisted: StateFlow<Boolean> = combine(
        currentProductId,
        wishlistRepository.wishlist
    ) { id, state ->
        if (id == null) return@combine false
        if (state is UiState.Success) {
            state.data.any { it.productId == id }
        } else {
            false
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val hasPriceAlert: StateFlow<Boolean> = combine(
        currentProductId,
        priceAlertRepository.alerts
    ) { id, state ->
        if (id == null) return@combine false
        if (state is UiState.Success) {
            state.data.any { it.productId == id }
        } else {
            false
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun loadProductDetails(productId: String) {
        currentProductId.value = productId
        viewModelScope.launch {
            _listingsState.value = UiState.Loading
            
            // Tải dữ liệu ban đầu cho Wishlist nếu cần
            if (wishlistRepository.wishlist.value is UiState.Idle) {
                wishlistRepository.refresh()
            }

            if (priceAlertRepository.alerts.value is UiState.Idle) {
                priceAlertRepository.refresh()
            }
            
            val listingsResult = platformProductRepository.getListingsByProductId(productId)
            _listingsState.value = listingsResult

            if (listingsResult is UiState.Success && listingsResult.data.isNotEmpty()) {
                val firstListing = listingsResult.data.bestPricedListing()
                
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
            if (isWishlisted.value) {
                wishlistRepository.remove(productId)
            } else {
                wishlistRepository.add(productId)
            }
            // Repository.add/remove đã tự động refresh flow, Detail sẽ cập nhật theo
        }
    }

    fun createPriceAlert(productId: String, targetPrice: Double) {
        viewModelScope.launch {
            _priceAlertState.value = UiState.Loading
            _priceAlertState.value = priceAlertRepository.create(productId, targetPrice)
        }
    }

    fun resetPriceAlertState() {
        _priceAlertState.value = UiState.Idle
    }
}

private fun List<PlatformListingDto>.bestPricedListing(): PlatformListingDto =
    minByOrNull { it.currentPrice.toDoubleOrNull() ?: Double.MAX_VALUE } ?: first()
