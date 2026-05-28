package android.app.producthunt.ui.viewmodel

import android.app.producthunt.data.remote.dto.WishlistResponse
import android.app.producthunt.data.repository.WishlistRepository
import android.app.producthunt.core.state.UiState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    init {
        refreshWishlist()
    }

    fun refreshWishlist() {
        viewModelScope.launch {
            repository.refresh()
        }
    }

    fun remove(productId: String) {
        viewModelScope.launch {
            repository.remove(productId)
        }
    }
    
    fun add(productId: String) {
        viewModelScope.launch {
            repository.add(productId)
        }
    }
}
