package com.edufelip.shared.ui.features.auth.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.edufelip.shared.data.auth.GoogleSignInLauncher
import com.edufelip.shared.ui.app.state.AmazingNoteAppState
import com.edufelip.shared.ui.features.auth.screens.LoginScreen
import com.edufelip.shared.ui.features.auth.screens.SignUpScreen
import com.edufelip.shared.ui.nav.AppRoutes

@Composable
fun LoginRoute(
    state: AmazingNoteAppState,
    googleSignInLauncher: GoogleSignInLauncher?,
    onNavigate: (AppRoutes) -> Unit,
    onBack: () -> Unit,
    showLocalSuccessSnackbar: Boolean = true,
) {
    val auth = state.authViewModel
    val uiState by auth.uiState.collectAsState()

    LoginScreen(
        state = uiState,
        onBack = onBack,
        googleSignInLauncher = googleSignInLauncher,
        onOpenSignUp = { onNavigate(AppRoutes.SignUp) },
        onLoginSuccess = { state.popToRoot() },
        showLocalSuccessSnackbar = showLocalSuccessSnackbar,
        onLogin = { email, password -> auth.loginWithEmail(email, password) },
        onGoogleSignIn = { token -> auth.signInWithGoogleToken(token) },
        onSendPasswordReset = { email -> auth.sendPasswordReset(email) },
        onClearError = { auth.clearError() },
        onClearMessage = { auth.clearMessage() },
        onSetError = { auth.setError(it) },
    )
}

@Composable
fun SignUpRoute(
    state: AmazingNoteAppState,
    onBack: () -> Unit,
) {
    val uiState by state.authViewModel.uiState.collectAsState()

    SignUpScreen(
        onBack = onBack,
        onSubmit = { email, password ->
            state.authViewModel.signUp(email, password)
            state.popToRoot()
        },
        loading = uiState.loading,
    )
}
