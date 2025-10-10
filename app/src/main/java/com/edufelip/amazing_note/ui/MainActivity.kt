package com.edufelip.amazing_note.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.edufelip.amazing_note.auth.FirebaseAuthServiceImpl
import com.edufelip.amazing_note.ui.viewmodels.KmpNoteViewModel
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.ui.AmazingNoteApp
import com.edufelip.shared.ui.settings.Settings
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val vm: KmpNoteViewModel by viewModels()
    private val authService by lazy { FirebaseAuthServiceImpl() }

    @Inject lateinit var settings: Settings

    @Inject lateinit var noteDb: NoteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val appVersion = runCatching {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "0.0.0"
        }.getOrElse { "0.0.0" }

        setContent {
            AmazingNoteApp(
                viewModel = vm,
                authService = authService,
                onRequestGoogleSignIn = { cb ->
                    lifecycleScope.launch {
                        try {
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(getString(com.edufelip.amazing_note.R.string.default_web_client_id))
                                .build()
                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(googleIdOption)
                                .build()
                            val credentialManager = CredentialManager.create(this@MainActivity)
                            val result = credentialManager.getCredential(
                                request = request,
                                context = this@MainActivity,
                            )
                            val credential = result.credential
                            val googleIdTokenCredential = when {
                                credential is androidx.credentials.CustomCredential &&
                                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                                    GoogleIdTokenCredential.createFrom(credential.data)
                                }
                                else -> null
                            }
                            val idToken = googleIdTokenCredential?.idToken
                            if (idToken.isNullOrBlank()) {
                                cb(false, "Missing Google ID token")
                            } else {
                                try {
                                    authService.signInWithGoogle(idToken)
                                    cb(true, null)
                                } catch (e: Exception) {
                                    cb(false, e.message ?: "Google sign-in failed")
                                }
                            }
                        } catch (e: Exception) {
                            cb(false, e.message ?: "Google sign-in canceled")
                        }
                    }
                },
                settings = settings,
                noteDatabase = noteDb,
                appVersion = appVersion,
            )
        }
    }
}
