package android.app.producthunt.data.repository

import android.app.producthunt.core.notification.FcmTokenRegistrar
import android.app.producthunt.data.local.TokenDataStore
import android.app.producthunt.data.remote.ApiErrorParser
import android.app.producthunt.data.remote.api.AuthApiService
import android.app.producthunt.data.remote.dto.*
import android.app.producthunt.core.state.UiState
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: AuthApiService,
    private val tokenDataStore: TokenDataStore,
    private val fcmTokenRegistrar: FcmTokenRegistrar,
) {
    suspend fun login(email: String, password: String): UiState<UserResponse> = try {
        val normalizedEmail = AuthAccountMatcher.normalizeEmail(email)
        tokenDataStore.clearTokens()

        val response = api.login(username = normalizedEmail, password = password)
        tokenDataStore.saveTokens(response.accessToken, response.refreshToken)

        val user = api.me()
        if (!AuthAccountMatcher.matchesLoginEmail(normalizedEmail, user.email)) {
            tokenDataStore.clearTokens()
            return UiState.Error("Authenticated account does not match the login email")
        }

        fcmTokenRegistrar.registerCurrentTokenIfAuthenticated()
        UiState.Success(user)
    } catch (e: Exception) {
        tokenDataStore.clearTokens()
        UiState.Error(ApiErrorParser.messageFrom(e, "Login failed"))
    }

    suspend fun register(email: String, password: String, fullName: String): UiState<UserResponse> = try {
        UiState.Success(api.register(RegisterRequest(AuthAccountMatcher.normalizeEmail(email), password, fullName.trim())))
    } catch (e: Exception) {
        UiState.Error(ApiErrorParser.messageFrom(e, "Registration failed"))
    }

    suspend fun forgotPassword(email: String): UiState<String> = try {
        val response = api.forgotPassword(ForgotPasswordRequest(AuthAccountMatcher.normalizeEmail(email)))
        UiState.Success(response.message ?: response.detail ?: "Link đặt lại mật khẩu đã được gửi đến email của bạn")
    } catch (e: Exception) {
        UiState.Error(ApiErrorParser.messageFrom(e, "Không thể gửi email đặt lại mật khẩu"))
    }

    suspend fun resetPassword(token: String, newPassword: String): UiState<String> = try {
        val response = api.resetPassword(ResetPasswordRequest(token.trim(), newPassword))
        UiState.Success(response.message ?: response.detail ?: "Đặt lại mật khẩu thành công")
    } catch (e: Exception) {
        UiState.Error(ApiErrorParser.messageFrom(e, "Không thể đặt lại mật khẩu"))
    }

    suspend fun me(): UiState<UserResponse> = try {
        UiState.Success(api.me())
    } catch (e: Exception) {
        UiState.Error(ApiErrorParser.messageFrom(e, "Failed to fetch profile"))
    }

    suspend fun hasValidSession(): Boolean {
        val hasAnyToken = tokenDataStore.getAccessToken() != null || tokenDataStore.getRefreshToken() != null
        if (!hasAnyToken) return false

        return try {
            api.me()
            true
        } catch (_: Exception) {
            when (refresh()) {
                is UiState.Success -> true
                else -> false
            }
        }
    }

    suspend fun refresh(): UiState<TokenResponse> = try {
        val refreshToken = tokenDataStore.getRefreshToken() ?: return UiState.Error("No refresh token")
        val response = api.refresh(RefreshTokenRequest(refreshToken))
        tokenDataStore.saveTokens(response.accessToken, response.refreshToken)
        UiState.Success(response)
    } catch (e: Exception) {
        UiState.Error(ApiErrorParser.messageFrom(e, "Token refresh failed"))
    }

    suspend fun restoreSession(): UiState<UserResponse?> = try {
        val refreshToken = tokenDataStore.getRefreshToken() ?: return UiState.Success(null)
        val response = api.refresh(RefreshTokenRequest(refreshToken))
        tokenDataStore.saveTokens(response.accessToken, response.refreshToken)
        val user = api.me()
        fcmTokenRegistrar.registerCurrentTokenIfAuthenticated()
        UiState.Success(user)
    } catch (e: Exception) {
        tokenDataStore.clearTokens()
        UiState.Success(null)
    }

    suspend fun logout() {
        fcmTokenRegistrar.unregisterCurrentTokenIfAuthenticated()
        tokenDataStore.clearTokens()
    }
}
