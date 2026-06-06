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

    private val _selectedListing = MutableStateFlow<PlatformListingDto?>(null)
    val selectedListing: StateFlow<PlatformListingDto?> = _selectedListing.asStateFlow()

    private val _priceAlertState = MutableStateFlow<UiState<PriceAlertResponse>>(UiState.Idle)
    val priceAlertState: StateFlow<UiState<PriceAlertResponse>> = _priceAlertState.asStateFlow()

    private val _wishlistActionState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val wishlistActionState: StateFlow<UiState<Boolean>> = _wishlistActionState.asStateFlow()

    private val currentProductId = MutableStateFlow<String?>(null)
    private val currentPlatformProductId = MutableStateFlow<String?>(null)

    // Quan sát danh sách yêu thích từ Repository để đồng bộ mọi nơi
    val isWishlisted: StateFlow<Boolean> = combine(
        currentPlatformProductId,
        wishlistRepository.wishlist
    ) { id, state ->
        if (id == null) return@combine false
        if (state is UiState.Success) {
            state.data.any { it.platformProductId == id }
        } else {
            false
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val hasPriceAlert: StateFlow<Boolean> = combine(
        currentPlatformProductId,
        priceAlertRepository.alerts
    ) { id, state ->
        if (id == null) return@combine false
        if (state is UiState.Success) {
            state.data.any { it.platformProductId == id }
        } else {
            false
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun loadProductDetails(productId: String, initialPlatformProductId: String? = null) {
        currentProductId.value = productId
        currentPlatformProductId.value = null
        _selectedListing.value = null
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
                val firstListing = listingsResult.data
                    .firstOrNull { it.id == initialPlatformProductId }
                    ?: listingsResult.data.bestPricedListing()
                _selectedListing.value = firstListing
                currentPlatformProductId.value = firstListing.id
                
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

    fun selectListing(listing: PlatformListingDto) {
        _selectedListing.value = listing
        currentPlatformProductId.value = listing.id
        val currentPrice = listing.currentPrice.toDoubleOrNull() ?: 0.0
        val originalPrice = listing.originalPrice?.toDoubleOrNull() ?: currentPrice
        loadPriceHistory(listing.id)
        loadAnalysis(listing.id, currentPrice, originalPrice)
    }

    private fun loadAnalysis(platformProductId: String, currentPrice: Double, originalPrice: Double) {
        viewModelScope.launch {
            _analysisState.value = UiState.Loading
            _analysisState.value = priceRecordRepository.getAnalysis(platformProductId, currentPrice, originalPrice)
        }
    }

    fun toggleWishlist(listing: PlatformListingDto) {
        viewModelScope.launch {
            val removing = isWishlisted.value
            _wishlistActionState.value = UiState.Loading
            val result = if (removing) {
                wishlistRepository.remove(listing.id)
            } else {
                wishlistRepository.add(platformProductId = listing.id, productId = listing.productId)
            }
            _wishlistActionState.value = when (result) {
                is UiState.Success -> UiState.Success(!removing)
                is UiState.Error -> UiState.Error(result.message)
                else -> UiState.Idle
            }
            // Repository.add/remove đã tự động refresh flow, Detail sẽ cập nhật theo
        }
    }

    fun createPriceAlert(listing: PlatformListingDto, targetPrice: Double) {
        viewModelScope.launch {
            _priceAlertState.value = UiState.Loading
            _priceAlertState.value = priceAlertRepository.create(
                platformProductId = listing.id,
                productId = listing.productId,
                targetPrice = targetPrice,
            )
        }
    }

    fun resetPriceAlertState() {
        _priceAlertState.value = UiState.Idle
    }

    fun resetWishlistActionState() {
        _wishlistActionState.value = UiState.Idle
    }
}

private fun List<PlatformListingDto>.bestPricedListing(): PlatformListingDto =
    minByOrNull { it.currentPrice.toDoubleOrNull() ?: Double.MAX_VALUE } ?: first()
