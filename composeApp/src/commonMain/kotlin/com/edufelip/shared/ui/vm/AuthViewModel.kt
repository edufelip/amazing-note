package com.edufelip.shared.ui.vm

import com.edufelip.shared.data.auth.AuthUser
import com.edufelip.shared.domain.usecase.AuthUseCases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val user: AuthUser? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val message: AuthMessage? = null,
)

sealed class AuthMessage {
    data object ResetEmailSent : AuthMessage()
    data object SignUpSuccess : AuthMessage()
}

class AuthViewModel(
    private val useCases: AuthUseCases,
    private val scope: CoroutineScope,
) {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        useCases.observeCurrentUser()
            .onEach { authUser ->
                _uiState.update { it.copy(user = authUser) }
            }
            .launchIn(scope)
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun setError(message: String) {
        _uiState.update { it.copy(error = message) }
    }

    fun loginWithEmail(email: String, password: String) {
        val current = _uiState.value
        if (current.loading) return
        _uiState.value = current.copy(loading = true, error = null, message = null)
        scope.launch(Dispatchers.Main) {
            try {
                useCases.login(email, password)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Login failed") }
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    fun logout() {
        if (_uiState.value.loading) return
        scope.launch(Dispatchers.Main) {
            try {
                useCases.logout()
            } catch (_: Exception) {}
        }
    }

    fun signInWithGoogleToken(idToken: String) {
        val current = _uiState.value
        if (current.loading) return
        _uiState.value = current.copy(loading = true, error = null, message = null)
        scope.launch(Dispatchers.Main) {
            try {
                useCases.signInWithGoogle(idToken)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Google sign-in failed") }
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    fun signUp(email: String, password: String) {
        val current = _uiState.value
        if (current.loading) return
        _uiState.value = current.copy(loading = true, error = null, message = null)
        scope.launch(Dispatchers.Main) {
            try {
                useCases.signUp(email, password)
                _uiState.update { it.copy(message = AuthMessage.SignUpSuccess) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Sign up failed") }
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        val current = _uiState.value
        if (current.loading) return
        _uiState.value = current.copy(loading = true, error = null, message = null)
        scope.launch(Dispatchers.Main) {
            try {
                useCases.sendPasswordReset(email)
                _uiState.update { it.copy(message = AuthMessage.ResetEmailSent) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to send reset email") }
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
        }
    }
}
