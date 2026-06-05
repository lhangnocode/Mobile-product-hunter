package android.app.producthunt.data.repository

import android.app.producthunt.core.state.UiState
import android.app.producthunt.data.remote.ApiErrorParser
import android.app.producthunt.data.remote.api.DeviceTokenApiService
import android.app.producthunt.data.remote.dto.DeviceTokenRequest
import android.net.Uri
import javax.inject.Inject

class DeviceTokenRepository @Inject constructor(
    private val api: DeviceTokenApiService,
) {
    suspend fun register(token: String): UiState<Unit> = try {
        api.register(DeviceTokenRequest(token = token))
        UiState.Success(Unit)
    } catch (e: Exception) {
        UiState.Error(ApiErrorParser.messageFrom(e, "Failed to register device token"))
    }

    suspend fun delete(token: String): UiState<Unit> = try {
        api.delete(Uri.encode(token))
        UiState.Success(Unit)
    } catch (e: Exception) {
        UiState.Error(ApiErrorParser.messageFrom(e, "Failed to delete device token"))
    }
}
