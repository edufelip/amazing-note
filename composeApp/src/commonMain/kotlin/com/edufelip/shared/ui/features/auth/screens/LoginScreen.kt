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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edufelip.shared.data.auth.GoogleSignInLauncher
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.continue_with_google
import com.edufelip.shared.resources.forgot_password
import com.edufelip.shared.resources.google_sign_in_canceled
import com.edufelip.shared.resources.login_headline
import com.edufelip.shared.resources.login_subheadline
import com.edufelip.shared.resources.reset_email_sent
import com.edufelip.shared.resources.sign_up_success
import com.edufelip.shared.ui.features.auth.components.ForgotPasswordDialog
import com.edufelip.shared.ui.features.auth.components.GoogleButton
import com.edufelip.shared.ui.features.auth.components.LoginCredentialsSection
import com.edufelip.shared.ui.features.auth.components.LoginFooter
import com.edufelip.shared.ui.features.auth.components.LoginHeader
import com.edufelip.shared.ui.features.auth.components.LoginIllustration
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import com.edufelip.shared.ui.vm.AuthUiState
import com.edufelip.shared.ui.vm.AuthViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    auth: AuthViewModel,
    onBack: () -> Unit,
    googleSignInLauncher: GoogleSignInLauncher? = null,
    onOpenSignUp: () -> Unit,
    onLoginSuccess: () -> Unit = onBack,
    showLocalSuccessSnackbar: Boolean = true,
) {
    val uiState by auth.uiState.collectAsState()
    LoginScreen(
        state = uiState,
        onBack = onBack,
        googleSignInLauncher = googleSignInLauncher,
        onOpenSignUp = onOpenSignUp,
        onLoginSuccess = onLoginSuccess,
        showLocalSuccessSnackbar = showLocalSuccessSnackbar,
        onLogin = { email, password -> auth.loginWithEmail(email, password) },
        onGoogleSignIn = { token -> auth.signInWithGoogleToken(token) },
        onSendPasswordReset = { email -> auth.sendPasswordReset(email) },
        onClearError = { auth.clearError() },
        onClearMessage = { auth.clearMessage() },
        onSetError = { auth.setError(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    state: AuthUiState,
    onBack: () -> Unit,
    googleSignInLauncher: GoogleSignInLauncher? = null,
    onOpenSignUp: () -> Unit,
    onLoginSuccess: () -> Unit,
    showLocalSuccessSnackbar: Boolean,
    onLogin: (String, String) -> Unit,
    onGoogleSignIn: (String) -> Unit,
    onSendPasswordReset: (String) -> Unit,
    onClearError: () -> Unit,
    onClearMessage: () -> Unit,
    onSetError: (String) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val loading = state.loading
    val error = state.error
    val user = state.user
    val message = state.message
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var forgotPasswordDialogVisible by rememberSaveable { mutableStateOf(false) }
    var resetEmail by rememberSaveable { mutableStateOf("") }
    var loginRequested by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(user, loginRequested) {
        if (loginRequested && user != null) {
            onLoginSuccess()
            loginRequested = false
        }
    }

    val resetEmailSentText = stringResource(Res.string.reset_email_sent)
    val signUpSuccessText = stringResource(Res.string.sign_up_success)

    LaunchedEffect(message) {
        when (message) {
            "RESET_EMAIL_SENT" -> {
                forgotPasswordDialogVisible = false
                snackbarHostState.showSnackbar(resetEmailSentText)
                onClearMessage()
            }

            "SIGN_UP_SUCCESS" -> if (showLocalSuccessSnackbar) {
                snackbarHostState.showSnackbar(signUpSuccessText)
                onClearMessage()
            }
        }
    }

    LaunchedEffect(error) {
        val currentError = error
        if (currentError != null) {
            snackbarHostState.showSnackbar(currentError)
            onClearError()
            loginRequested = false
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LoginHeader(onBack = onBack)
            Spacer(modifier = Modifier.height(32.dp))
            LoginIllustration(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f),
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(Res.string.login_headline),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.login_subheadline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(28.dp))

            LoginCredentialsSection(
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                passwordVisible = passwordVisible,
                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                loading = loading,
                onSubmit = {
                    loginRequested = true
                    onLogin(email.trim(), password)
                },
            )
            Spacer(modifier = Modifier.height(20.dp))
            val googleCanceledText = stringResource(Res.string.google_sign_in_canceled)
            GoogleButton(
                text = stringResource(Res.string.continue_with_google),
                enabled = googleSignInLauncher != null && !loading,
                onClick = {
                    val launcher = googleSignInLauncher ?: return@GoogleButton
                    scope.launch {
                        val result = launcher.signIn()
                        when {
                            !result.idToken.isNullOrBlank() -> {
                                loginRequested = true
                                onGoogleSignIn(result.idToken)
                            }
                            !result.errorMessage.isNullOrBlank() -> {
                                onSetError(result.errorMessage)
                                snackbarHostState.showSnackbar(result.errorMessage)
                                loginRequested = false
                            }
                            else -> snackbarHostState.showSnackbar(googleCanceledText)
                        }
                    }
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {
                    resetEmail = email.trim()
                    forgotPasswordDialogVisible = true
                },
                enabled = !loading,
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    text = stringResource(Res.string.forgot_password),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            LoginFooter(onOpenSignUp = onOpenSignUp)
        }

        if (forgotPasswordDialogVisible) {
            ForgotPasswordDialog(
                email = resetEmail,
                loading = loading,
                onEmailChange = { resetEmail = it },
                onDismiss = { forgotPasswordDialogVisible = false },
                onSubmit = {
                    resetEmail = resetEmail.trim()
                    if (resetEmail.isNotEmpty()) {
                        onSendPasswordReset(resetEmail)
                    }
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
            onLoginSuccess = {},
            showLocalSuccessSnackbar = true,
            onLogin = { _, _ -> },
            onGoogleSignIn = {},
            onSendPasswordReset = {},
            onClearError = {},
            onClearMessage = {},
            onSetError = {},
        )
    }
}
