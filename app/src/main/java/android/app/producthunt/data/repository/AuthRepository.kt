package android.app.producthunt.data.repository

import android.app.producthunt.data.local.TokenDataStore
import android.app.producthunt.data.remote.api.AuthApiService
import android.app.producthunt.data.remote.dto.*
import android.app.producthunt.domain.UiState
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val api: AuthApiService,
    private val tokenDataStore: TokenDataStore,
) {
    suspend fun login(email: String, password: String): UiState<TokenResponse> = try {
        val response = api.login(username = email, password = password)
        tokenDataStore.saveTokens(response.accessToken, response.refreshToken)
        UiState.Success(response)
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Login failed")
    }

    suspend fun register(email: String, password: String, fullName: String): UiState<UserResponse> = try {
        UiState.Success(api.register(RegisterRequest(email, password, fullName)))
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Registration failed")
    }

    suspend fun forgotPassword(email: String): UiState<String> = try {
        val response = api.forgotPassword(ForgotPasswordRequest(email))
        UiState.Success(response.message ?: response.detail ?: "Mã OTP đã được gửi")
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Không thể gửi OTP")
    }

    suspend fun verifyOtp(email: String, otp: String): UiState<String> = try {
        val response = api.verifyOtp(VerifyOtpRequest(email, otp))
        UiState.Success(response.message ?: response.detail ?: "Xác thực thành công")
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Mã OTP không hợp lệ")
    }

    suspend fun resetPassword(email: String, otp: String, newPassword: String): UiState<String> = try {
        val response = api.resetPassword(ResetPasswordRequest(email, otp, newPassword))
        UiState.Success(response.message ?: response.detail ?: "Đặt lại mật khẩu thành công")
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Không thể đặt lại mật khẩu")
    }

    suspend fun me(): UiState<UserResponse> = try {
        UiState.Success(api.me())
    } catch (e: Exception) {
        UiState.Error(e.message ?: "Failed to fetch profile")
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
        UiState.Error(e.message ?: "Token refresh failed")
    }

    suspend fun restoreSession(): UiState<Boolean> = try {
        val refreshToken = tokenDataStore.getRefreshToken() ?: return UiState.Success(false)
        val response = api.refresh(RefreshTokenRequest(refreshToken))
        tokenDataStore.saveTokens(response.accessToken, response.refreshToken)
        UiState.Success(true)
    } catch (e: Exception) {
        tokenDataStore.clearTokens()
        UiState.Success(false)
    }

    suspend fun logout() = tokenDataStore.clearTokens()
}
