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
) {
    LoginScreen(
        auth = state.authViewModel,
        onBack = onBack,
        googleSignInLauncher = googleSignInLauncher,
        onOpenSignUp = { onNavigate(AppRoutes.SignUp) },
        onLoginSuccess = { state.popToRoot() },
        showLocalSuccessSnackbar = true,
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
