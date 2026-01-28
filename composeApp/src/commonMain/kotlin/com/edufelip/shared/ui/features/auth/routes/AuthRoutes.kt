package com.edufelip.shared.ui.features.auth.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.edufelip.shared.data.auth.AppleSignInLauncher
import com.edufelip.shared.data.auth.GoogleSignInLauncher
import com.edufelip.shared.ui.app.state.AmazingNoteAppState
import com.edufelip.shared.ui.features.auth.screens.LoginScreen
import com.edufelip.shared.ui.features.auth.screens.SignUpScreen
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.util.lifecycle.collectWithLifecycle
import com.edufelip.shared.ui.vm.NoteUiViewModel

@Composable
fun LoginRoute(
    state: AmazingNoteAppState,
    viewModel: NoteUiViewModel,
    googleSignInLauncher: GoogleSignInLauncher?,
    appleSignInLauncher: AppleSignInLauncher?,
    onNavigate: (AppRoutes) -> Unit,
    onBack: () -> Unit,
    showLocalSuccessToast: Boolean = true,
) {
    val auth = state.authViewModel
    val uiState by auth.uiState.collectWithLifecycle()

    LoginScreen(
        state = uiState,
        onBack = onBack,
        googleSignInLauncher = googleSignInLauncher,
        appleSignInLauncher = appleSignInLauncher,
        onOpenSignUp = { onNavigate(AppRoutes.DetailDestination.SignUp) },
        showLocalSuccessToast = showLocalSuccessToast,
        onLogin = { email, password -> auth.loginWithEmail(email, password) },
        onGoogleSignIn = { idToken, accessToken -> auth.signInWithGoogleToken(idToken, accessToken) },
        onAppleSignIn = { idToken, rawNonce, fullName, email ->
            auth.signInWithAppleToken(
                idToken = idToken,
                rawNonce = rawNonce,
                fullName = fullName,
                email = email,
            )
        },
        onSendPasswordReset = { email -> auth.sendPasswordReset(email) },
        onClearError = { auth.clearError() },
        onSetError = { auth.setError(it) },
        events = auth.events,
        onLoginSuccess = {
            if (!state.popBack()) {
                state.setRoot(AppRoutes.TabDestination.Notes)
            }
        },
    )
}

@Composable
fun SignUpRoute(
    state: AmazingNoteAppState,
    onBack: () -> Unit,
) {
    val authViewModel = state.authViewModel
    val uiState by authViewModel.uiState.collectWithLifecycle()

    SignUpScreen(
        onBack = onBack,
        onSubmit = { name, email, password, confirm ->
            authViewModel.signUp(name, email, password, confirm)
        },
        loading = uiState.loading,
        error = uiState.error,
        events = authViewModel.events,
        onSignUpSuccess = { state.setRoot(AppRoutes.TabDestination.Notes) },
    )
}
