package com.edufelip.shared.auth

import androidx.compose.runtime.Composable

data class GoogleSignInResult(
    val idToken: String?,
    val errorMessage: String?,
)

interface GoogleSignInLauncher {
    suspend fun signIn(): GoogleSignInResult
}

@Composable
expect fun rememberGoogleSignInLauncher(config: GoogleSignInConfig = GoogleSignInConfig()): GoogleSignInLauncher?

data class GoogleSignInConfig(
    val androidServerClientId: String? = null,
)
