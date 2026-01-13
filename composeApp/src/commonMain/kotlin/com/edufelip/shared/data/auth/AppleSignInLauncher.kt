package com.edufelip.shared.data.auth

import androidx.compose.runtime.Composable

data class AppleSignInResult(
    val idToken: String?,
    val rawNonce: String?,
    val fullName: String?,
    val email: String?,
    val errorMessage: String?,
)

interface AppleSignInLauncher {
    suspend fun signIn(): AppleSignInResult
}

@Composable
expect fun rememberAppleSignInLauncher(): AppleSignInLauncher?
