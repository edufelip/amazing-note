package com.edufelip.shared.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AuthController(
    private val service: AuthService,
    private val scope: CoroutineScope
) {
    private val _user = MutableStateFlow<AuthUser?>(null)
    val user: StateFlow<AuthUser?> = _user.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        service.currentUser
            .onEach { _user.value = it }
            .launchIn(scope)
    }

    fun clearError() { _error.value = null }
    fun clearMessage() { _message.value = null }

    fun loginWithEmail(email: String, password: String) {
        if (_loading.value) return
        _loading.value = true
        _error.value = null
        _message.value = null
        scope.launch(Dispatchers.Main) {
            try {
                service.signInWithEmailPassword(email, password)
            } catch (e: Exception) {
                _error.value = e.message ?: "Login failed"
            } finally {
                _loading.value = false
            }
        }
    }

    fun logout() {
        if (_loading.value) return
        scope.launch(Dispatchers.Main) {
            try { service.signOut() } catch (_: Exception) {}
        }
    }

    fun signUp(email: String, password: String) {
        if (_loading.value) return
        _loading.value = true
        _error.value = null
        _message.value = null
        scope.launch(Dispatchers.Main) {
            try {
                service.signUpWithEmailPassword(email, password)
                _message.value = "SIGN_UP_SUCCESS"
            } catch (e: Exception) {
                _error.value = e.message ?: "Sign up failed"
            } finally {
                _loading.value = false
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (_loading.value) return
        _loading.value = true
        _error.value = null
        _message.value = null
        scope.launch(Dispatchers.Main) {
            try {
                service.sendPasswordResetEmail(email)
                _message.value = "RESET_EMAIL_SENT"
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to send reset email"
            } finally {
                _loading.value = false
            }
        }
    }
}
