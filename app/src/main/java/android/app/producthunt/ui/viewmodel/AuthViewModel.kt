package android.app.producthunt.ui.viewmodel

import android.app.producthunt.data.remote.dto.UserResponse
import android.app.producthunt.data.repository.AuthRepository
import android.app.producthunt.data.repository.PriceAlertRepository
import android.app.producthunt.data.repository.WishlistRepository
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
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val wishlistRepository: WishlistRepository,
    private val priceAlertRepository: PriceAlertRepository,
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<UserResponse>>(UiState.Idle)
    val loginState: StateFlow<UiState<UserResponse>> = _loginState.asStateFlow()

    private val _sessionState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val sessionState: StateFlow<UiState<Boolean>> = _sessionState.asStateFlow()

    private val _registerState = MutableStateFlow<UiState<UserResponse>>(UiState.Idle)
    val registerState: StateFlow<UiState<UserResponse>> = _registerState.asStateFlow()

    private val _startupState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val startupState: StateFlow<UiState<Boolean>> = _startupState.asStateFlow()

    private val _currentUserState = MutableStateFlow<UiState<UserResponse>>(UiState.Idle)
    val currentUserState: StateFlow<UiState<UserResponse>> = _currentUserState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val forgotPasswordState: StateFlow<UiState<String>> = _forgotPasswordState.asStateFlow()

    private val _verifyOtpState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val verifyOtpState: StateFlow<UiState<String>> = _verifyOtpState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val resetPasswordState: StateFlow<UiState<String>> = _resetPasswordState.asStateFlow()

    fun restoreSession() {
        viewModelScope.launch {
            _startupState.value = UiState.Loading
            when (val result = repository.restoreSession()) {
                is UiState.Success -> {
                    val user = result.data
                    if (user != null) {
                        _currentUserState.value = UiState.Success(user)
                        _startupState.value = UiState.Success(true)
                    } else {
                        clearUserScopedState()
                        _startupState.value = UiState.Success(false)
                    }
                }
                is UiState.Error -> {
                    clearUserScopedState()
                    _startupState.value = UiState.Success(false)
                }
                else -> _startupState.value = UiState.Success(false)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            clearUserScopedState()
            when (val result = repository.login(email, password)) {
                is UiState.Success -> {
                    _currentUserState.value = UiState.Success(result.data)
                    _loginState.value = result
                    _startupState.value = UiState.Success(true)
                }
                is UiState.Error -> {
                    clearUserScopedState()
                    _loginState.value = result
                    _startupState.value = UiState.Success(false)
                }
                else -> _loginState.value = result
            }
        }
    }

    fun register(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            _registerState.value = repository.register(email, password, fullName)
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = UiState.Loading
            _forgotPasswordState.value = repository.forgotPassword(email)
        }
    }

    fun resetPassword(token: String, newPassword: String) {
        viewModelScope.launch {
            _resetPasswordState.value = UiState.Loading
            val result = repository.resetPassword(token, newPassword)
            if (result is UiState.Success) {
                repository.logout()
                clearUserScopedState()
                _startupState.value = UiState.Success(false)
                _sessionState.value = UiState.Success(false)
            }
            _resetPasswordState.value = result
        }
    }

    fun checkSession() {
        viewModelScope.launch {
            _sessionState.value = UiState.Loading
            _sessionState.value = if (repository.hasValidSession()) {
                when (val userResult = repository.me()) {
                    is UiState.Success -> {
                        _currentUserState.value = userResult
                        UiState.Success(true)
                    }
                    is UiState.Error -> {
                        repository.logout()
                        clearUserScopedState()
                        UiState.Success(false)
                    }
                    else -> UiState.Success(false)
                }
            } else {
                clearUserScopedState()
                UiState.Success(false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            clearUserScopedState()
            _loginState.value = UiState.Idle
            _sessionState.value = UiState.Success(false)
            _startupState.value = UiState.Success(false)
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUserState.value = UiState.Loading
            _currentUserState.value = repository.me()
        }
    }

    fun resetLoginState() {
        _loginState.value = UiState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = UiState.Idle
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = UiState.Idle
    }

    fun resetVerifyOtpState() {
        _verifyOtpState.value = UiState.Idle
    }

    fun resetResetPasswordState() {
        _resetPasswordState.value = UiState.Idle
    }

    private fun clearUserScopedState() {
        _currentUserState.value = UiState.Idle
        wishlistRepository.clear()
        priceAlertRepository.clear()
    }
}
