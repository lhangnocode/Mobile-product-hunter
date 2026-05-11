package android.app.producthunt.ui.viewmodel

import android.app.producthunt.data.remote.dto.TokenResponse
import android.app.producthunt.data.remote.dto.UserResponse
import android.app.producthunt.data.repository.AuthRepository
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
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<TokenResponse>>(UiState.Idle)
    val loginState: StateFlow<UiState<TokenResponse>> = _loginState.asStateFlow()

    private val _sessionState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val sessionState: StateFlow<UiState<Boolean>> = _sessionState.asStateFlow()

    private val _registerState = MutableStateFlow<UiState<UserResponse>>(UiState.Idle)
    val registerState: StateFlow<UiState<UserResponse>> = _registerState.asStateFlow()

    private val _startupState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val startupState: StateFlow<UiState<Boolean>> = _startupState.asStateFlow()

    fun restoreSession() {
        viewModelScope.launch {
            _startupState.value = UiState.Loading
            _startupState.value = repository.restoreSession()
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            _loginState.value = repository.login(email, password)
        }
    }

    fun register(email: String, password: String, fullName: String) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading
            _registerState.value = repository.register(email, password, fullName)
        }
    }

    fun checkSession() {
        viewModelScope.launch {
            _sessionState.value = UiState.Loading
            _sessionState.value = if (repository.hasValidSession()) {
                UiState.Success(true)
            } else {
                UiState.Success(false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _loginState.value = UiState.Idle
            _sessionState.value = UiState.Success(false)
        }
    }

    fun resetLoginState() {
        _loginState.value = UiState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = UiState.Idle
    }
}
