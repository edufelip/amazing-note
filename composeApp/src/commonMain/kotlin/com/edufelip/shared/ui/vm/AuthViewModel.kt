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
import com.edufelip.shared.util.debugLog
import kotlinx.coroutines.CancellationException
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
    val error: AuthError? = null,
)

sealed class AuthEvent {
    data object LoginSuccess : AuthEvent()
    data object SignUpSuccess : AuthEvent()
    data class PasswordResetSent(val email: String) : AuthEvent()
}

sealed interface AuthError {
    data object GenericValidation : AuthError
    data object Network : AuthError
    data object InvalidCredentials : AuthError
    data class Custom(val message: String) : AuthError
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
        _uiState.update { it.copy(error = AuthError.Custom(message)) }
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
                publishError(result.exceptionOrNull()!!)
            }
            stopLoading()
        }
    }

    fun logout() {
        launchInScope {
            val result = runCatching { useCases.logout() }
            if (result.isSuccess) {
                _uiState.update { it.copy(user = null, loading = false, error = null, isUserResolved = true) }
            } else {
                publishError(result.exceptionOrNull()!!)
            }
        }
    }

    fun signInWithGoogleToken(idToken: String, accessToken: String?) {
        if (_uiState.value.loading) return
        if (idToken.isBlank()) {
            SecurityLogger.logValidationFailure(
                flow = "google_sign_in",
                field = "token",
                reason = "EMPTY",
                rawSample = "",
            )
            _uiState.update { it.copy(error = AuthError.Custom("Missing Google token")) }
            return
        }
        setLoading()
        launchInScope {
            val result = runCatching { useCases.signInWithGoogle(idToken, accessToken) }
            if (result.isSuccess) {
                _events.emit(AuthEvent.LoginSuccess)
            } else {
                val error = result.exceptionOrNull()!!
                debugLog("Firebase Google sign-in failed: ${error.message ?: "no message"}")
                publishError(error)
            }
            stopLoading()
        }
    }

    fun signUp(name: String, email: String, password: String, confirmPassword: String) {
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
                useCases.signUp(name, validation.email.sanitized, validation.password.sanitized)
            }
            if (result.isSuccess) {
                runCatching {
                    useCases.updateUserName(name)
                }
                _uiState.update { current ->
                    val updatedUser = current.user?.copy(displayName = name)
                        ?: current.user
                    current.copy(user = updatedUser)
                }
                _events.emit(AuthEvent.SignUpSuccess)
            } else {
                publishError(result.exceptionOrNull()!!)
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
                publishError(result.exceptionOrNull()!!)
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

    private fun publishError(error: Throwable) {
        if (error is CancellationException) throw error
        val resolvedError = when {
            error.isInvalidCredentialError() -> AuthError.InvalidCredentials
            error.isNetworkError() -> AuthError.Network
            else -> error.firstNonBlankMessage()?.let { AuthError.Custom(it) } ?: AuthError.GenericValidation
        }
        _uiState.update { it.copy(error = resolvedError) }
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
        val authError = message?.let { AuthError.Custom(it) } ?: AuthError.GenericValidation
        _uiState.update { it.copy(error = authError) }
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
        val message = emailErrorMessage(result.error)
        val authError = message?.let { AuthError.Custom(it) } ?: AuthError.GenericValidation
        _uiState.update { it.copy(error = authError) }
    }

    private fun emailErrorMessage(error: EmailValidationError?): String? = when (error) {
        EmailValidationError.REQUIRED -> "Email is required"
        EmailValidationError.INVALID_FORMAT -> "Enter a valid email address"
        null -> null
    }

    private fun passwordErrorMessage(error: PasswordValidationError?): String? = when (error) {
        PasswordValidationError.REQUIRED -> "Password is required"
        PasswordValidationError.TOO_SHORT -> "Password must be at least 8 characters"
        PasswordValidationError.MISSING_UPPER,
        PasswordValidationError.MISSING_LOWER,
        PasswordValidationError.MISSING_DIGIT,
        PasswordValidationError.MISSING_SYMBOL,
        -> "Password must include upper, lower, digit, and symbol"
        null -> null
    }

    private fun handlePasswordConfirmationFailure(result: PasswordConfirmationResult) {
        val message = confirmationErrorMessage(result.error)
        SecurityLogger.logValidationFailure(
            flow = "sign_up",
            field = "confirm_password",
            reason = result.error?.name ?: "unknown",
            rawSample = result.sanitized,
        )
        val authError = message?.let { AuthError.Custom(it) } ?: AuthError.GenericValidation
        _uiState.update { it.copy(error = authError) }
    }

    private fun confirmationErrorMessage(error: PasswordConfirmationError?): String? = when (error) {
        PasswordConfirmationError.REQUIRED -> "Confirm your password"
        PasswordConfirmationError.MISMATCH -> "Passwords must match"
        null -> null
    }
}

private fun Throwable.firstNonBlankMessage(): String? {
    var current: Throwable? = this
    while (current != null) {
        val message = current.message
        if (!message.isNullOrBlank()) return message
        current = current.cause
    }
    return null
}

private fun Throwable.isInvalidCredentialError(): Boolean = messageMatches(invalidCredentialHints)

private fun Throwable.isNetworkError(): Boolean {
    if (messageMatches(networkErrorHints)) return true
    var current: Throwable? = this
    while (current != null) {
        val name = current::class.simpleName?.lowercase()
        if (name != null && (
                "network" in name ||
                    "timeout" in name ||
                    "ioexception" in name
                )
        ) {
            return true
        }
        current = current.cause
    }
    return false
}

private fun Throwable.messageMatches(hints: List<String>): Boolean {
    var current: Throwable? = this
    while (current != null) {
        val message = current.message?.lowercase()
        if (message != null && hints.any { it in message }) {
            return true
        }
        current = current.cause
    }
    return false
}

private val invalidCredentialHints = listOf(
    "password is invalid",
    "invalid password",
    "invalid credential",
    "credential is incorrect",
    "no user record",
    "user not found",
)

private val networkErrorHints = listOf(
    "network error",
    "network request failed",
    "network unreachable",
    "unable to resolve host",
    "connection reset",
    "timed out",
    "timeout",
)
