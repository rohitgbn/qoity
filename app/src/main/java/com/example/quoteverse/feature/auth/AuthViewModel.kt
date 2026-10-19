package com.example.quoteverse.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quoteverse.core.model.User
import com.example.quoteverse.core.result.Resource
import com.example.quoteverse.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isUsernameAvailable: Boolean? = null,
    val usernameCheckLoading: Boolean = false,
    val errorMessage: String? = null,
    val resetEmailSent: Boolean = false
)

sealed class AuthUiEvent {
    object NavigateToHome : AuthUiEvent()
    data class ShowSnackbar(val message: String) : AuthUiEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthUiEvent>()
    val events: SharedFlow<AuthUiEvent> = _events.asSharedFlow()

    val currentUser = authRepository.getCurrentUserFlow()
    val isUserLoggedIn get() = authRepository.isUserLoggedIn
    val currentUserId get() = authRepository.currentUserId

    fun checkUsername(username: String) {
        if (username.length < 3) {
            _uiState.update { it.copy(isUsernameAvailable = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(usernameCheckLoading = true) }
            authRepository.checkUsernameAvailability(username).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(isUsernameAvailable = result.data, usernameCheckLoading = false) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(usernameCheckLoading = false) }
                    }
                    is Resource.Loading -> {}
                }
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter both email and password") }
            return
        }
        viewModelScope.launch {
            authRepository.signInWithEmail(email, password).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _events.emit(AuthUiEvent.NavigateToHome)
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                        _events.emit(AuthUiEvent.ShowSnackbar(result.message))
                    }
                }
            }
        }
    }

    fun register(email: String, password: String, username: String, displayName: String) {
        if (email.isBlank() || password.isBlank() || username.isBlank() || displayName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "All fields are required") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
            return
        }
        viewModelScope.launch {
            authRepository.signUpWithEmail(email, password, username, displayName).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _events.emit(AuthUiEvent.NavigateToHome)
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                        _events.emit(AuthUiEvent.ShowSnackbar(result.message))
                    }
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            authRepository.signInWithGoogle(idToken).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _events.emit(AuthUiEvent.NavigateToHome)
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                        _events.emit(AuthUiEvent.ShowSnackbar(result.message))
                    }
                }
            }
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your email address") }
            return
        }
        viewModelScope.launch {
            authRepository.sendPasswordResetEmail(email).collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false, resetEmailSent = true) }
                        _events.emit(AuthUiEvent.ShowSnackbar("Password reset link sent to your email!"))
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                        _events.emit(AuthUiEvent.ShowSnackbar(result.message))
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
