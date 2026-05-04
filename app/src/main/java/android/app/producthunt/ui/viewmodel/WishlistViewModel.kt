package android.app.producthunt.ui.viewmodel

import android.app.producthunt.data.remote.dto.WishlistResponse
import android.app.producthunt.data.repository.WishlistRepository
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
class WishlistViewModel @Inject constructor(
    private val repository: WishlistRepository,
) : ViewModel() {

    private val _wishlistState = MutableStateFlow<UiState<List<WishlistResponse>>>(UiState.Idle)
    val wishlistState: StateFlow<UiState<List<WishlistResponse>>> = _wishlistState.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val actionState: StateFlow<UiState<Unit>> = _actionState.asStateFlow()

    init {
        loadWishlist()
    }

    fun loadWishlist() {
        viewModelScope.launch {
            _wishlistState.value = UiState.Loading
            _wishlistState.value = repository.get()
        }
    }

    fun add(productId: String) {
        viewModelScope.launch {
            val result = repository.add(productId)
            if (result is UiState.Success) loadWishlist()
            else _actionState.value = result as UiState.Error
        }
    }

    fun remove(productId: String) {
        viewModelScope.launch {
            val result = repository.remove(productId)
            if (result is UiState.Success) loadWishlist()
            else _actionState.value = result as UiState.Error
        }
    }
}
