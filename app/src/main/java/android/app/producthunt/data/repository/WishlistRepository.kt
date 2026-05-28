package android.app.producthunt.data.repository

import android.app.producthunt.data.remote.api.WishlistApiService
import android.app.producthunt.data.remote.dto.WishListCreate
import android.app.producthunt.data.remote.dto.WishlistResponse
import android.app.producthunt.core.state.UiState
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WishlistRepository @Inject constructor(
    private val api: WishlistApiService,
) {
    private val _wishlist = MutableStateFlow<UiState<List<WishlistResponse>>>(UiState.Idle)
    val wishlist: StateFlow<UiState<List<WishlistResponse>>> = _wishlist.asStateFlow()

    suspend fun refresh() {
        if (_wishlist.value !is UiState.Success) {
            _wishlist.value = UiState.Loading
        }
        try {
            val response = api.get()
            _wishlist.value = UiState.Success(response.toWishlistList())
        } catch (e: Exception) {
            if (_wishlist.value !is UiState.Success) {
                _wishlist.value = UiState.Error(e.message ?: "Failed to load wishlist")
            }
        }
    }

    suspend fun add(productId: String): UiState<WishlistResponse> = try {
        val result = api.add(WishListCreate(productId))
        refresh() 
        UiState.Success(result)
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to add to wishlist")
    }

    suspend fun remove(productId: String): UiState<Unit> = try {
        api.remove(productId)
        refresh()
        UiState.Success(Unit)
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to remove from wishlist")
    }

    fun clear() {
        _wishlist.value = UiState.Idle
    }

    private val wishlistListType = object : TypeToken<List<WishlistResponse>>() {}.type

    private fun JsonElement.toWishlistList(): List<WishlistResponse> {
        val listJson = when {
            isJsonArray -> asJsonArray
            isJsonObject -> {
                val obj = asJsonObject
                listOf("data", "items", "results", "wishlist", "wishlists", "wish_lists")
                    .firstNotNullOfOrNull { key -> obj.get(key)?.takeIf { it.isJsonArray }?.asJsonArray }
                    ?: throw IllegalStateException("Unexpected wishlist response format")
            }
            else -> throw IllegalStateException("Unexpected wishlist response")
        }
        return Gson().fromJson(listJson, wishlistListType)
    }
}
