package com.edufelip.shared.ui.features.auth.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.edufelip.shared.data.auth.GoogleSignInLauncher
import com.edufelip.shared.domain.validation.EmailValidationError
import com.edufelip.shared.domain.validation.PasswordValidationError
import com.edufelip.shared.domain.validation.validateEmail
import com.edufelip.shared.domain.validation.validatePassword
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.auth_generic_validation_error
import com.edufelip.shared.resources.auth_network_error
import com.edufelip.shared.resources.continue_with_google
import com.edufelip.shared.resources.email_invalid_format
import com.edufelip.shared.resources.email_required
import com.edufelip.shared.resources.forgot_password
import com.edufelip.shared.resources.forgot_password_invalid_email
import com.edufelip.shared.resources.forgot_password_try_again
import com.edufelip.shared.resources.google_sign_in_canceled
import com.edufelip.shared.resources.login_error_invalid_credentials
import com.edufelip.shared.resources.login_headline
import com.edufelip.shared.resources.login_rate_limit
import com.edufelip.shared.resources.login_subheadline
import com.edufelip.shared.resources.password_required
import com.edufelip.shared.resources.password_requirements
import com.edufelip.shared.resources.reset_email_sent
import com.edufelip.shared.resources.sign_up_success
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.effects.toast.rememberToastController
import com.edufelip.shared.ui.effects.toast.show
import com.edufelip.shared.ui.features.auth.components.ForgotPasswordDialog
import com.edufelip.shared.ui.features.auth.components.ForgotPasswordSuccessDialog
import com.edufelip.shared.ui.features.auth.components.GoogleButton
import com.edufelip.shared.ui.features.auth.components.LoginCredentialsSection
import com.edufelip.shared.ui.features.auth.components.LoginFooter
import com.edufelip.shared.ui.features.auth.components.LoginHeader
import com.edufelip.shared.ui.features.auth.components.LoginIllustration
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import com.edufelip.shared.ui.util.OnSystemBack
import com.edufelip.shared.ui.util.platform.currentEpochMillis
import com.edufelip.shared.ui.util.security.AuthRateLimiter
import com.edufelip.shared.ui.util.security.SecurityLogger
import com.edufelip.shared.ui.vm.AuthError
import com.edufelip.shared.ui.vm.AuthEvent
import com.edufelip.shared.ui.vm.AuthUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(
    state: AuthUiState,
    onBack: () -> Unit,
    googleSignInLauncher: GoogleSignInLauncher? = null,
    onOpenSignUp: () -> Unit,
    showLocalSuccessToast: Boolean,
    onLogin: (String, String) -> Unit,
    onGoogleSignIn: (String, String?) -> Unit,
    onSendPasswordReset: (String) -> Unit,
    onClearError: () -> Unit,
    onSetError: (String) -> Unit,
    events: SharedFlow<AuthEvent>,
    onLoginSuccess: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val loading = state.loading
    val error = state.error
    val errorText = when (error) {
        AuthError.GenericValidation -> stringResource(Res.string.auth_generic_validation_error)
        AuthError.Network -> stringResource(Res.string.auth_network_error)
        AuthError.InvalidCredentials -> stringResource(Res.string.login_error_invalid_credentials)
        is AuthError.Custom -> error.message
        null -> null
    }
    val scope = rememberCoroutineScope()
    val toastController = rememberToastController()
    val scrollState = rememberScrollState()
    var forgotPasswordDialogVisible by rememberSaveable { mutableStateOf(false) }
    var forgotPasswordEmail by rememberSaveable { mutableStateOf("") }
    var forgotPasswordEmailError by rememberSaveable { mutableStateOf<String?>(null) }
    var forgotPasswordSubmitting by rememberSaveable { mutableStateOf(false) }
    var forgotPasswordPending by rememberSaveable { mutableStateOf(false) }
    var forgotPasswordSuccessVisible by rememberSaveable { mutableStateOf(false) }
    val tokens = designTokens()
    var emailHasFocus by remember { mutableStateOf(false) }
    var passwordHasFocus by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }
    var emailImmediateValidation by remember { mutableStateOf(false) }
    var passwordImmediateValidation by remember { mutableStateOf(false) }
    var emailValidation by remember { mutableStateOf(validateEmail(email)) }
    var passwordValidation by remember { mutableStateOf(validatePassword(password)) }
    val rateLimiter = remember { AuthRateLimiter() }
    var lockoutUntil by remember { mutableStateOf(0L) }
    var lockoutTicker by remember { mutableStateOf(0L) }
    LaunchedEffect(email, emailHasFocus, emailImmediateValidation) {
        if (!emailHasFocus || emailImmediateValidation) {
            emailValidation = validateEmail(email)
            if (emailImmediateValidation) emailImmediateValidation = false
        } else {
            val snapshot = email
            delay(300)
            if (snapshot == email && emailHasFocus) {
                emailValidation = validateEmail(snapshot)
            }
        }
    }
    LaunchedEffect(password, passwordHasFocus, passwordImmediateValidation) {
        if (!passwordHasFocus || passwordImmediateValidation) {
            passwordValidation = validatePassword(password)
            if (passwordImmediateValidation) passwordImmediateValidation = false
        } else {
            val snapshot = password
            delay(300)
            if (snapshot == password && passwordHasFocus) {
                passwordValidation = validatePassword(snapshot)
            }
        }
    }

    val showEmailError = emailTouched && !emailHasFocus && emailValidation.error != null
    val showPasswordError = passwordTouched && !passwordHasFocus && passwordValidation.error != null
    val emailErrorText = if (showEmailError) {
        when (emailValidation.error) {
            EmailValidationError.REQUIRED -> stringResource(Res.string.email_required)
            EmailValidationError.INVALID_FORMAT -> stringResource(Res.string.email_invalid_format)
            null -> null
        }
    } else {
        null
    }
    val passwordErrorText = if (showPasswordError) {
        when (passwordValidation.error) {
            PasswordValidationError.REQUIRED -> stringResource(Res.string.password_required)
            PasswordValidationError.TOO_SHORT,
            PasswordValidationError.MISSING_UPPER,
            PasswordValidationError.MISSING_LOWER,
            PasswordValidationError.MISSING_DIGIT,
            PasswordValidationError.MISSING_SYMBOL,
            -> stringResource(Res.string.password_requirements)
            null -> null
        }
    } else {
        null
    }
    LaunchedEffect(lockoutUntil) {
        if (lockoutUntil <= 0L) {
            lockoutTicker = 0L
            return@LaunchedEffect
        }
        while (true) {
            val remaining = rateLimiter.lockoutRemaining(currentEpochMillis())
            lockoutTicker = remaining
            if (remaining <= 0L) break
            delay(1000)
        }
    }
    val lockoutRemainingMillis = lockoutTicker
    val isLockedOut = lockoutRemainingMillis > 0L
    val lockoutSeconds = ((lockoutRemainingMillis + 999) / 1000).toInt()
    val isSubmitEnabled = emailValidation.isValid && passwordValidation.isValid && !loading && !isLockedOut
    val attemptLogin: () -> Unit = l@{
        val now = currentEpochMillis()
        if (!rateLimiter.canAttempt(now)) {
            val remaining = rateLimiter.lockoutRemaining(now)
            lockoutTicker = remaining
            if (remaining > 0) {
                SecurityLogger.logRateLimit("login", remaining)
            }
            return@l
        }
        emailTouched = true
        passwordTouched = true
        emailImmediateValidation = true
        passwordImmediateValidation = true
        val latestEmailValidation = validateEmail(email)
        val latestPasswordValidation = validatePassword(password)
        emailValidation = latestEmailValidation
        passwordValidation = latestPasswordValidation
        val ready = latestEmailValidation.isValid && latestPasswordValidation.isValid && !loading
        if (ready) {
            onLogin(latestEmailValidation.sanitized, latestPasswordValidation.sanitized)
            return@l
        }
    }

    val resetEmailSentText = stringResource(Res.string.reset_email_sent)
    val signUpSuccessText = stringResource(Res.string.sign_up_success)
    val forgotPasswordInvalidEmailText = stringResource(Res.string.forgot_password_invalid_email)
    val forgotPasswordTryAgainText = stringResource(Res.string.forgot_password_try_again)

    LaunchedEffect(events, showLocalSuccessToast) {
        events.collect { event ->
            when (event) {
                AuthEvent.LoginSuccess -> {
                    rateLimiter.reset()
                    lockoutUntil = 0L
                    lockoutTicker = 0L
                    onLoginSuccess()
                }

                is AuthEvent.PasswordResetSent -> {
                    if (forgotPasswordPending) {
                        forgotPasswordPending = false
                        forgotPasswordSubmitting = false
                        forgotPasswordEmailError = null
                        forgotPasswordDialogVisible = false
                        forgotPasswordSuccessVisible = true
                    } else {
                        toastController.show(resetEmailSentText)
                    }
                }

                AuthEvent.SignUpSuccess -> if (showLocalSuccessToast) {
                    toastController.show(signUpSuccessText)
                }
            }
        }
    }

    val submitPasswordReset: () -> Unit = {
        val validation = validateEmail(forgotPasswordEmail.trim())
        if (!validation.isValid) {
            forgotPasswordEmailError = forgotPasswordInvalidEmailText
        } else {
            forgotPasswordSubmitting = true
            forgotPasswordPending = true
            onSendPasswordReset(validation.sanitized)
        }
    }

    LaunchedEffect(error) {
        val currentError = error ?: return@LaunchedEffect
        onClearError()
        if (forgotPasswordPending) {
            forgotPasswordPending = false
            forgotPasswordSubmitting = false
            forgotPasswordEmailError = forgotPasswordTryAgainText
        } else {
            val now = currentEpochMillis()
            lockoutUntil = rateLimiter.registerFailure(now)
            lockoutTicker = rateLimiter.lockoutRemaining(now)
            if (lockoutTicker > 0L) {
                SecurityLogger.logRateLimit("login", lockoutTicker)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { LoginHeader(onBack = onBack) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = tokens.spacing.xl, vertical = tokens.spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(tokens.spacing.lg))
            LoginIllustration(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f),
            )
            Spacer(modifier = Modifier.height(tokens.spacing.xl))
            Text(
                text = stringResource(Res.string.login_headline),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(tokens.spacing.sm))
            Text(
                text = stringResource(Res.string.login_subheadline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(tokens.spacing.xl))

            LoginCredentialsSection(
                email = email,
                onEmailChange = {
                    email = it
                    if (!emailTouched) emailTouched = true
                },
                password = password,
                onPasswordChange = {
                    password = it
                    if (!passwordTouched) passwordTouched = true
                },
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                loading = loading,
                onSubmit = attemptLogin,
                showError = errorText != null && !isLockedOut,
                errorMessage = errorText ?: stringResource(Res.string.login_error_invalid_credentials),
                emailErrorMessage = emailErrorText,
                passwordErrorMessage = passwordErrorText,
                isSubmitEnabled = isSubmitEnabled,
                onEmailFocusChanged = { focused ->
                    emailHasFocus = focused
                    if (focused) {
                        emailTouched = true
                    } else if (emailTouched) {
                        emailImmediateValidation = true
                    }
                },
                onPasswordFocusChanged = { focused ->
                    passwordHasFocus = focused
                    if (focused) {
                        passwordTouched = true
                    } else if (passwordTouched) {
                        passwordImmediateValidation = true
                    }
                },
            )
            if (isLockedOut) {
                Spacer(modifier = Modifier.height(tokens.spacing.sm))
                Text(
                    text = stringResource(Res.string.login_rate_limit, lockoutSeconds),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(tokens.spacing.lg))
            GoogleButton(
                text = stringResource(Res.string.continue_with_google),
                enabled = googleSignInLauncher != null && !loading && !isLockedOut,
                onClick = {
                    val launcher = googleSignInLauncher ?: return@GoogleButton
                    scope.launch {
                        val result = launcher.signIn()
                        when {
                            !result.idToken.isNullOrBlank() -> {
                                onGoogleSignIn(result.idToken, result.accessToken)
                            }
                            !result.errorMessage.isNullOrBlank() -> {
                                onSetError(result.errorMessage)
                                toastController.show(result.errorMessage)
                            }
                            else -> Unit // user dismissed; no toast
                        }
                    }
                },
            )
            Spacer(modifier = Modifier.height(tokens.spacing.lg))
            TextButton(
                onClick = {
                    forgotPasswordEmail = email.trim()
                    forgotPasswordEmailError = null
                    forgotPasswordSubmitting = false
                    forgotPasswordPending = false
                    forgotPasswordDialogVisible = true
                },
                enabled = !loading && !isLockedOut,
                contentPadding = PaddingValues(Dp.Hairline),
            ) {
                Text(
                    text = stringResource(Res.string.forgot_password),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(tokens.spacing.xl))
            LoginFooter(onOpenSignUp = onOpenSignUp)
        }

        if (forgotPasswordDialogVisible) {
            OnSystemBack {
                forgotPasswordDialogVisible = false
                forgotPasswordSubmitting = false
                forgotPasswordPending = false
                forgotPasswordEmailError = null
            }
            ForgotPasswordDialog(
                email = forgotPasswordEmail,
                errorMessage = forgotPasswordEmailError,
                loading = forgotPasswordSubmitting,
                onEmailChange = {
                    forgotPasswordEmail = it
                    forgotPasswordEmailError = null
                },
                onDismiss = {
                    forgotPasswordDialogVisible = false
                    forgotPasswordSubmitting = false
                    forgotPasswordPending = false
                    forgotPasswordEmailError = null
                },
                onSubmit = submitPasswordReset,
            )
        }

        if (forgotPasswordSuccessVisible) {
            OnSystemBack {
                forgotPasswordSuccessVisible = false
            }
            ForgotPasswordSuccessDialog(
                email = forgotPasswordEmail,
                onDismiss = {
                    forgotPasswordSuccessVisible = false
                },
            )
        }
    }
}

@Composable
@Preview
@DevicePreviews
private fun LoginScreenPreview() {
    DevicePreviewContainer {
        LoginScreen(
            state = AuthUiState(),
            onBack = {},
            googleSignInLauncher = null,
            onOpenSignUp = {},
            showLocalSuccessToast = true,
            onLogin = { _, _ -> },
            onGoogleSignIn = { _, _ -> },
            onSendPasswordReset = {},
            onClearError = {},
            onSetError = {},
            events = MutableSharedFlow(),
            onLoginSuccess = {},
        )
    }
}
