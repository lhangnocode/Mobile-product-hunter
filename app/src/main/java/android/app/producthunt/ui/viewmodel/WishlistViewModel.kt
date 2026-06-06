package android.app.producthunt.ui.viewmodel

import android.app.producthunt.data.remote.dto.WishlistResponse
import android.app.producthunt.data.repository.WishlistRepository
import android.app.producthunt.core.state.UiState
import android.app.producthunt.data.remote.dto.PriceAlertResponse
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val repository: WishlistRepository,
) : ViewModel() {

    // Observe wishlist directly from repository for real-time sync across screens
    val wishlistState: StateFlow<UiState<List<WishlistResponse>>> = repository.wishlist
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Idle)

    private val _removeState = MutableStateFlow<UiState<Int>>(UiState.Idle)
    val removeState: StateFlow<UiState<Int>> = _removeState.asStateFlow()

    init {
        refreshWishlist()
    }

    fun refreshWishlist() {
        viewModelScope.launch {
            repository.refresh()
        }
    }

    fun remove(platformProductId: String) {
        viewModelScope.launch {
            _removeState.value = UiState.Loading
            _removeState.value = when (val result = repository.remove(platformProductId)) {
                is UiState.Success -> UiState.Success(1)
                is UiState.Error -> UiState.Error(result.message)
                else -> UiState.Idle
            }
        }
    }
    
    fun add(platformProductId: String, productId: String? = null) {
        viewModelScope.launch {
            repository.add(platformProductId, productId)
        }
    }

    fun removeAll() {
        viewModelScope.launch {
            val ids = (repository.wishlist.value as? UiState.Success)
                ?.data?.map { it.platformProductId } ?: return@launch
            _removeState.value = UiState.Loading
            var removedCount = 0
            ids.forEach { repository.remove(it) }
            removedCount = ids.size
            _removeState.value = UiState.Success(removedCount)
        }
    }

    fun resetRemoveState() {
        _removeState.value = UiState.Idle
    }
}
