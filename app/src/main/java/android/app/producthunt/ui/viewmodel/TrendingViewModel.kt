package android.app.producthunt.ui.viewmodel

import android.app.producthunt.data.remote.dto.TrendingDealResponse
import android.app.producthunt.data.remote.dto.detailPlatformProductId
import android.app.producthunt.data.remote.dto.PriceAlertResponse
import android.app.producthunt.data.repository.PlatformProductRepository
import android.app.producthunt.core.state.UiState
import android.app.producthunt.data.repository.PriceAlertRepository
import android.app.producthunt.data.repository.WishlistRepository
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
    private val wishlistRepository: WishlistRepository,
    private val priceAlertRepository: PriceAlertRepository,
) : ViewModel() {

    private val _trendingState = MutableStateFlow<UiState<List<TrendingDealResponse>>>(UiState.Idle)
    val trendingState: StateFlow<UiState<List<TrendingDealResponse>>> = _trendingState.asStateFlow()

    private val _wishlistedIds = MutableStateFlow<Set<String>>(emptySet())
    val wishlistedIds: StateFlow<Set<String>> = _wishlistedIds.asStateFlow()

    private val _priceAlertIds = MutableStateFlow<Set<String>>(emptySet())
    val priceAlertIds: StateFlow<Set<String>> = _priceAlertIds.asStateFlow()

    private val _wishlistActionState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val wishlistActionState: StateFlow<UiState<Boolean>> = _wishlistActionState.asStateFlow()

    private val _priceAlertState = MutableStateFlow<UiState<PriceAlertResponse>>(UiState.Idle)
    val priceAlertState: StateFlow<UiState<PriceAlertResponse>> = _priceAlertState.asStateFlow()

    init {
        loadTrending()
        loadWishlistedIds()
        loadPriceAlertIds()
    }

    fun loadTrending(limit: Int = 20) {
        viewModelScope.launch {
            _trendingState.value = UiState.Loading
            _trendingState.value = repository.getTrending(limit)
        }
    }

    private fun loadWishlistedIds() {
        viewModelScope.launch {
            wishlistRepository.refresh()
            wishlistRepository.wishlist.collect { state ->
                if (state is UiState.Success) {
                    _wishlistedIds.value = state.data.map { it.platformProductId }.toSet()
                }
            }
        }
    }

    private fun loadPriceAlertIds() {
        viewModelScope.launch {
            priceAlertRepository.refresh()
            priceAlertRepository.alerts.collect { state ->
                if (state is UiState.Success) {
                    _priceAlertIds.value = state.data.map { it.platformProductId }.toSet()
                }
            }
        }
    }

    fun toggleWishlist(deal: TrendingDealResponse) {
        val platformProductId = deal.detailPlatformProductId
        viewModelScope.launch {
            val removing = platformProductId in _wishlistedIds.value
            _wishlistActionState.value = UiState.Loading
            val result = if (removing) {
                wishlistRepository.remove(platformProductId)
            } else {
                wishlistRepository.add(
                    platformProductId = platformProductId,
                    productId = deal.productId,
                )
            }
            if (result is UiState.Success) {
                if (removing) {
                    _wishlistedIds.value -= platformProductId
                } else {
                    _wishlistedIds.value += platformProductId
                }
            }
            _wishlistActionState.value = when (result) {
                is UiState.Success -> UiState.Success(!removing)
                is UiState.Error -> UiState.Error(result.message)
                else -> UiState.Idle
            }
        }
    }

    fun createPriceAlert(deal: TrendingDealResponse, targetPrice: Double) {
        val platformProductId = deal.detailPlatformProductId
        viewModelScope.launch {
            _priceAlertState.value = UiState.Loading
            val result = priceAlertRepository.create(
                platformProductId = platformProductId,
                productId = deal.productId,
                targetPrice = targetPrice,
            )
            if (result is UiState.Success) {
                _priceAlertIds.value += platformProductId
            }
            _priceAlertState.value = result
        }
    }

    fun resetWishlistActionState() {
        _wishlistActionState.value = UiState.Idle
    }

    fun resetPriceAlertState() {
        _priceAlertState.value = UiState.Idle
    }
}
