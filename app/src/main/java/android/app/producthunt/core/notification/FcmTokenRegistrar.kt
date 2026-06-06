package android.app.producthunt.core.notification

import android.app.producthunt.core.state.UiState
import android.app.producthunt.data.local.FcmTokenDataStore
import android.app.producthunt.data.local.TokenDataStore
import android.app.producthunt.data.repository.DeviceTokenRepository
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class FcmTokenRegistrar @Inject constructor(
    private val deviceTokenRepository: DeviceTokenRepository,
    private val tokenDataStore: TokenDataStore,
    private val fcmTokenDataStore: FcmTokenDataStore,
) {
    suspend fun registerCurrentTokenIfAuthenticated() {
        if (!hasAuthenticatedSession()) return

        val token = fetchCurrentFirebaseToken()
        if (token.isNullOrBlank()) {
            Log.w(TAG, "Firebase returned an empty FCM token")
            return
        }

        registerTokenIfAuthenticated(token)
    }

    suspend fun registerTokenIfAuthenticated(token: String) {
        if (token.isBlank()) return

        if (!hasAuthenticatedSession()) {
            fcmTokenDataStore.saveLastToken(token)
            return
        }

        when (val result = deviceTokenRepository.register(token)) {
            is UiState.Success -> {
                fcmTokenDataStore.saveLastToken(token)
                Log.i(TAG, "FCM token registered with backend: ${token.preview()}")
            }
            is UiState.Error -> Log.w(TAG, "FCM token registration failed: ${result.message}")
            else -> Unit
        }
    }

    suspend fun unregisterCurrentTokenIfAuthenticated() {
        val token = fcmTokenDataStore.getLastToken() ?: fetchCurrentFirebaseToken()
        if (!token.isNullOrBlank() && hasAuthenticatedSession()) {
            when (val result = deviceTokenRepository.delete(token)) {
                is UiState.Error -> Log.w(TAG, "FCM token deletion failed: ${result.message}")
                is UiState.Success -> Log.i(TAG, "FCM token deactivated on backend: ${token.preview()}")
                else -> Unit
            }
        }
        fcmTokenDataStore.clearLastToken()
    }

    private suspend fun hasAuthenticatedSession(): Boolean =
        tokenDataStore.getAccessToken() != null

    private suspend fun fetchCurrentFirebaseToken(): String? =
        suspendCancellableCoroutine { continuation ->
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    if (continuation.isActive) continuation.resume(token)
                }
                .addOnFailureListener { throwable ->
                    Log.w(TAG, "Failed to fetch FCM token", throwable)
                    if (continuation.isActive) continuation.resume(null)
                }
                .addOnCanceledListener {
                    if (continuation.isActive) continuation.resume(null)
                }
        }

    private companion object {
        private const val TAG = "FcmTokenRegistrar"
    }
}

private fun String.preview(): String =
    if (length <= 12) this else "${take(6)}...${takeLast(6)}"
