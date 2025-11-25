package com.edufelip.shared.data.auth

import androidx.compose.runtime.Composable

@Composable
actual fun rememberGoogleSignInLauncher(config: GoogleSignInConfig): GoogleSignInLauncher? = object : GoogleSignInLauncher {
    override suspend fun signIn(): GoogleSignInResult = GoogleSignInResult(
        idToken = null,
        errorMessage = "Google Sign-In is not available on iOS in this build.",
    )
}
