package com.edufelip.shared.data.auth

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
actual fun rememberGoogleSignInLauncher(config: GoogleSignInConfig): GoogleSignInLauncher? {
    val context = LocalContext.current
    val activity = context as? ComponentActivity ?: return null
    val serverClientId = config.androidServerClientId ?: return null
    return remember(activity, serverClientId) {
        AndroidGoogleSignInLauncher(activity, serverClientId)
    }
}

private class AndroidGoogleSignInLauncher(
    private val activity: ComponentActivity,
    private val serverClientId: String,
) : GoogleSignInLauncher {
    override suspend fun signIn(): GoogleSignInResult = try {
        val credentialManager = CredentialManager.create(activity)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = withContext(Dispatchers.Main) {
            credentialManager.getCredential(activity, request)
        }

        val credential = result.credential
        val idToken = when {
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                GoogleIdTokenCredential.createFrom(credential.data).idToken
            }

            else -> null
        }

        if (!idToken.isNullOrBlank()) {
            GoogleSignInResult(idToken = idToken, accessToken = null, errorMessage = null)
        } else {
            GoogleSignInResult(idToken = null, accessToken = null, errorMessage = "Missing Google ID token")
        }
    } catch (e: GetCredentialException) {
        GoogleSignInResult(
            idToken = null,
            accessToken = null,
            errorMessage = e.message ?: "Google sign-in canceled",
        )
    } catch (e: Exception) {
        GoogleSignInResult(idToken = null, accessToken = null, errorMessage = e.message ?: "Google sign-in failed")
    }
}
