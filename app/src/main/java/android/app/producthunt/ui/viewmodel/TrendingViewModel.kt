package android.app.producthunt.ui.viewmodel

import android.app.producthunt.data.remote.dto.TrendingDealResponse
import android.app.producthunt.data.repository.PlatformProductRepository
import android.app.producthunt.core.state.UiState
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
) : ViewModel() {

    private val _trendingState = MutableStateFlow<UiState<List<TrendingDealResponse>>>(UiState.Idle)
    val trendingState: StateFlow<UiState<List<TrendingDealResponse>>> = _trendingState.asStateFlow()

    private val _wishlistedIds = MutableStateFlow<Set<String>>(emptySet())
    val wishlistedIds: StateFlow<Set<String>> = _wishlistedIds.asStateFlow()

    init {
        loadTrending()
        loadWishlistedIds()
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
                    _wishlistedIds.value = state.data.map { it.productId }.toSet()
                }
            }
        }
    }

    fun toggleWishlist(productId: String) {
        viewModelScope.launch {
            if (productId in _wishlistedIds.value) {
                wishlistRepository.remove(productId)
                _wishlistedIds.value -= productId
            } else {
                wishlistRepository.add(productId)
                _wishlistedIds.value += productId
            }
        }
    }
}
