package com.edufelip.shared.ui.features.auth.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    onNavigate: (AppRoutes) -> Unit,
    onBack: () -> Unit,
    showLocalSuccessToast: Boolean = true,
) {
    val auth = state.authViewModel
    val syncManager = state.environment.notesSyncManager
    val uiState by auth.uiState.collectWithLifecycle()

    LoginScreen(
        state = uiState,
        onBack = onBack,
        googleSignInLauncher = googleSignInLauncher,
        onOpenSignUp = { onNavigate(AppRoutes.SignUp) },
        showLocalSuccessToast = showLocalSuccessToast,
        onLogin = { email, password -> auth.loginWithEmail(email, password) },
        onGoogleSignIn = { idToken, accessToken -> auth.signInWithGoogleToken(idToken, accessToken) },
        onSendPasswordReset = { email -> auth.sendPasswordReset(email) },
        onClearError = { auth.clearError() },
        onSetError = { auth.setError(it) },
        events = auth.events,
        onLoginSuccess = {
            // Login is reached from Settings; return there so the authenticated state is visible.
            val popped = state.popBack()
            if (!popped || state.currentRoute != AppRoutes.Settings) {
                state.setRoot(AppRoutes.Settings)
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
        onSignUpSuccess = { state.setRoot(AppRoutes.Notes) },
    )
}
