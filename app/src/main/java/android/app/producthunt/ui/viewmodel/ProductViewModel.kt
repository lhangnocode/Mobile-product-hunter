package android.app.producthunt.ui.viewmodel

import android.app.producthunt.data.remote.dto.ProductResponse
import android.app.producthunt.data.remote.dto.SearchCompareResponse
import android.app.producthunt.data.remote.dto.SearchPaginatedResponse
import android.app.producthunt.data.repository.ProductRepository
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
class ProductViewModel @Inject constructor(
    private val repository: ProductRepository,
) : ViewModel() {

    private val _productsState = MutableStateFlow<UiState<List<ProductResponse>>>(UiState.Idle)
    val productsState: StateFlow<UiState<List<ProductResponse>>> = _productsState.asStateFlow()

    private val _searchState = MutableStateFlow<UiState<SearchPaginatedResponse>>(UiState.Idle)
    val searchState: StateFlow<UiState<SearchPaginatedResponse>> = _searchState.asStateFlow()

    private val _compareState = MutableStateFlow<UiState<SearchCompareResponse>>(UiState.Idle)
    val compareState: StateFlow<UiState<SearchCompareResponse>> = _compareState.asStateFlow()

    init {
        getProducts()
    }

    fun getProducts() {
        viewModelScope.launch {
            _productsState.value = UiState.Loading
            _productsState.value = repository.getProducts()
        }
    }

    fun search(q: String, page: Int = 1) {
        viewModelScope.launch {
            _searchState.value = UiState.Loading
            _searchState.value = repository.search(q, page)
        }
    }

    fun compare(q: String) {
        viewModelScope.launch {
            _compareState.value = UiState.Loading
            _compareState.value = repository.compare(q)
        }
    }

    fun resetSearch() {
        _searchState.value = UiState.Idle
    }
}
