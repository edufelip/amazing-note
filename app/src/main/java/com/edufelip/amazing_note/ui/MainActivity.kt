package com.edufelip.amazing_note.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.edufelip.amazing_note.ui.viewmodels.KmpNoteViewModel
import com.edufelip.amazing_note.auth.FirebaseAuthServiceImpl
import com.edufelip.shared.i18n.ProvideAndroidStrings
import com.edufelip.shared.ui.AmazingNoteApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val vm: KmpNoteViewModel by viewModels()
    private lateinit var googleSignInClient: GoogleSignInClient
    private val authService by lazy { FirebaseAuthServiceImpl() }
    private var pendingGoogleCallback: ((Boolean, String?) -> Unit)? = null

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data: Intent? = result.data
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.result
            val token = account.idToken
            if (token != null) {
                // Sign in with Firebase using the Google ID token
                lifecycleScope.launchWhenResumed {
                    try {
                        authService.signInWithGoogle(token)
                        pendingGoogleCallback?.invoke(true, null)
                    } catch (e: Exception) {
                        pendingGoogleCallback?.invoke(false, e.message ?: "Google sign-in failed")
                    } finally {
                        pendingGoogleCallback = null
                    }
                }
            } else {
                pendingGoogleCallback?.invoke(false, "Missing Google ID token")
                pendingGoogleCallback = null
            }
        } catch (e: Exception) {
            // User cancelled or error occurred
            pendingGoogleCallback?.invoke(false, e.message ?: "Google sign-in canceled")
            pendingGoogleCallback = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.edufelip.amazing_note.R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            ProvideAndroidStrings {
                AmazingNoteApp(
                    viewModel = vm,
                    authService = authService,
                    onRequestGoogleSignIn = { cb ->
                        pendingGoogleCallback = cb
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    }
                )
            }
        }
    }
}
