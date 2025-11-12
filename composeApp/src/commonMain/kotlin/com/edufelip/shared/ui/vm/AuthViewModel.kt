package com.edufelip.shared.ui.vm

import com.edufelip.shared.data.auth.AuthUser
import com.edufelip.shared.domain.usecase.AuthUseCases
import com.edufelip.shared.domain.validation.CredentialValidationResult
import com.edufelip.shared.domain.validation.EmailValidationError
import com.edufelip.shared.domain.validation.EmailValidationResult
import com.edufelip.shared.domain.validation.PasswordConfirmationError
import com.edufelip.shared.domain.validation.PasswordConfirmationResult
import com.edufelip.shared.domain.validation.PasswordValidationError
import com.edufelip.shared.ui.util.security.SecurityLogger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class AuthUiState(
    val user: AuthUser? = null,
    val isUserResolved: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null,
)

sealed class AuthEvent {
    data object LoginSuccess : AuthEvent()
    data object SignUpSuccess : AuthEvent()
    data class PasswordResetSent(val email: String) : AuthEvent()
}

class AuthViewModel(
    private val useCases: AuthUseCases,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
) : SharedViewModel(dispatcher) {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    private val _events = MutableSharedFlow<AuthEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    init {
        useCases.observeCurrentUser()
            .onEach { authUser ->
                _uiState.update { it.copy(user = authUser, isUserResolved = true) }
            }
            .collectInScope()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun setError(message: String) {
        _uiState.update { it.copy(error = message) }
    }

    fun loginWithEmail(email: String, password: String) {
        if (_uiState.value.loading) return
        val validation = useCases.validateCredentials(email, password)
        if (!validation.isValid) {
            handleCredentialValidationFailure(flow = "login", validation = validation)
            return
        }
        setLoading()
        launchInScope {
            val result = runCatching {
                useCases.login(validation.email.sanitized, validation.password.sanitized)
            }
            if (result.isSuccess) {
                _events.emit(AuthEvent.LoginSuccess)
            } else {
                publishError(result.exceptionOrNull()!!, "Login failed")
            }
            stopLoading()
        }
    }

    fun logout() {
        if (_uiState.value.loading) return
        launchInScope {
            try {
                useCases.logout()
            } catch (_: Exception) {}
        }
    }

    fun signInWithGoogleToken(idToken: String) {
        if (_uiState.value.loading) return
        if (idToken.isBlank()) {
            SecurityLogger.logValidationFailure(
                flow = "google_sign_in",
                field = "token",
                reason = "EMPTY",
                rawSample = "",
            )
            _uiState.update { it.copy(error = "Missing Google token") }
            return
        }
        setLoading()
        launchInScope {
            val result = runCatching { useCases.signInWithGoogle(idToken) }
            if (result.isSuccess) {
                _events.emit(AuthEvent.LoginSuccess)
            } else {
                publishError(result.exceptionOrNull()!!, "Google sign-in failed")
            }
            stopLoading()
        }
    }

    fun signUp(email: String, password: String, confirmPassword: String) {
        if (_uiState.value.loading) return
        val validation = useCases.validateCredentials(email, password)
        if (!validation.isValid) {
            handleCredentialValidationFailure(flow = "sign_up", validation = validation)
            return
        }
        val confirmation = useCases.validatePasswordConfirmation(
            password = validation.password.sanitized,
            confirmation = confirmPassword,
        )
        if (!confirmation.isValid) {
            handlePasswordConfirmationFailure(confirmation)
            return
        }
        setLoading()
        launchInScope {
            val result = runCatching {
                useCases.signUp(validation.email.sanitized, validation.password.sanitized)
            }
            if (result.isSuccess) {
                _events.emit(AuthEvent.SignUpSuccess)
            } else {
                publishError(result.exceptionOrNull()!!, "Sign up failed")
            }
            stopLoading()
        }
    }

    fun sendPasswordReset(email: String) {
        if (_uiState.value.loading) return
        val validation = useCases.validateEmail(email)
        if (!validation.isValid) {
            handleEmailValidationFailure(flow = "forgot_password", result = validation)
            return
        }
        setLoading()
        launchInScope {
            val result = runCatching { useCases.sendPasswordReset(validation.sanitized) }
            if (result.isSuccess) {
                _events.emit(AuthEvent.PasswordResetSent(validation.sanitized))
            } else {
                publishError(result.exceptionOrNull()!!, "Failed to send reset email")
            }
            stopLoading()
        }
    }

    private fun setLoading() {
        _uiState.update { it.copy(loading = true, error = null) }
    }

    private fun stopLoading() {
        _uiState.update { it.copy(loading = false) }
    }

    private fun publishError(error: Throwable, fallbackMessage: String) {
        _uiState.update { it.copy(error = error.message ?: fallbackMessage) }
    }

    private fun handleCredentialValidationFailure(
        flow: String,
        validation: CredentialValidationResult,
    ) {
        var message: String? = null
        if (!validation.email.isValid) {
            SecurityLogger.logValidationFailure(
                flow = flow,
                field = "email",
                reason = validation.email.error?.name ?: "unknown",
                rawSample = validation.email.sanitized,
            )
            message = emailErrorMessage(validation.email.error)
        }
        if (!validation.password.isValid) {
            SecurityLogger.logValidationFailure(
                flow = flow,
                field = "password",
                reason = validation.password.error?.name ?: "unknown",
                rawSample = "***",
            )
            if (message == null) {
                message = passwordErrorMessage(validation.password.error)
            }
        }
        _uiState.update { it.copy(error = message ?: GENERIC_VALIDATION_ERROR) }
    }

    private fun handleEmailValidationFailure(
        flow: String,
        result: EmailValidationResult,
    ) {
        SecurityLogger.logValidationFailure(
            flow = flow,
            field = "email",
            reason = result.error?.name ?: "unknown",
            rawSample = result.sanitized,
        )
        _uiState.update { it.copy(error = emailErrorMessage(result.error)) }
    }

    private fun emailErrorMessage(error: EmailValidationError?): String = when (error) {
        EmailValidationError.REQUIRED -> "Email is required"
        EmailValidationError.INVALID_FORMAT -> "Enter a valid email address"
        null -> GENERIC_VALIDATION_ERROR
    }

    private fun passwordErrorMessage(error: PasswordValidationError?): String = when (error) {
        PasswordValidationError.REQUIRED -> "Password is required"
        PasswordValidationError.TOO_SHORT -> "Password must be at least 8 characters"
        PasswordValidationError.MISSING_UPPER,
        PasswordValidationError.MISSING_LOWER,
        PasswordValidationError.MISSING_DIGIT,
        PasswordValidationError.MISSING_SYMBOL,
        -> "Password must include upper, lower, digit, and symbol"
        null -> GENERIC_VALIDATION_ERROR
    }

    private fun handlePasswordConfirmationFailure(result: PasswordConfirmationResult) {
        val message = confirmationErrorMessage(result.error)
        SecurityLogger.logValidationFailure(
            flow = "sign_up",
            field = "confirm_password",
            reason = result.error?.name ?: "unknown",
            rawSample = result.sanitized,
        )
        _uiState.update { it.copy(error = message) }
    }

    private fun confirmationErrorMessage(error: PasswordConfirmationError?): String = when (error) {
        PasswordConfirmationError.REQUIRED -> "Confirm your password"
        PasswordConfirmationError.MISMATCH -> "Passwords must match"
        null -> GENERIC_VALIDATION_ERROR
    }

    private companion object {
        const val GENERIC_VALIDATION_ERROR = "Check your credentials and try again"
    }
}
