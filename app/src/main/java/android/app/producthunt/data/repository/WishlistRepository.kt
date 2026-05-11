package android.app.producthunt.data.repository

import android.app.producthunt.data.remote.api.WishlistApiService
import android.app.producthunt.data.remote.dto.WishListCreate
import android.app.producthunt.data.remote.dto.WishlistResponse
import android.app.producthunt.domain.UiState
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

class WishlistRepository @Inject constructor(
    private val api: WishlistApiService,
) {
    suspend fun get(): UiState<List<WishlistResponse>> = try {
        UiState.Success(api.get().toWishlistList())
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to load wishlist")
    }

    suspend fun add(productId: String): UiState<WishlistResponse> = try {
        UiState.Success(api.add(WishListCreate(productId)))
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to add to wishlist")
    }

    suspend fun remove(productId: String): UiState<Unit> = try {
        api.remove(productId)
        UiState.Success(Unit)
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to remove from wishlist")
    }
}

private val wishlistListType = object : TypeToken<List<WishlistResponse>>() {}.type

private fun JsonElement.toWishlistList(): List<WishlistResponse> {
    val listJson = when {
        isJsonArray -> asJsonArray
        isJsonObject -> {
            val obj = asJsonObject
            listOf("data", "items", "results", "wishlist", "wishlists", "wish_lists")
                .firstNotNullOfOrNull { key -> obj.get(key)?.takeIf { it.isJsonArray }?.asJsonArray }
                ?: throw IllegalStateException(obj.errorMessage() ?: "Unexpected wishlist response")
        }
        else -> throw IllegalStateException("Unexpected wishlist response")
    }

    return Gson().fromJson(listJson, wishlistListType)
}

private fun com.google.gson.JsonObject.errorMessage(): String? =
    listOf("detail", "message", "error")
        .firstNotNullOfOrNull { key ->
            get(key)
                ?.takeIf { it.isJsonPrimitive }
                ?.asString
        }
