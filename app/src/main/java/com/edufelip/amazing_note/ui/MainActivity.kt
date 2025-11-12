package com.edufelip.amazing_note.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.edufelip.amazing_note.R
import com.edufelip.shared.data.auth.GoogleSignInConfig
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.di.getSharedKoin
import com.edufelip.shared.ui.AmazingNoteApp
import com.edufelip.shared.ui.settings.Settings
import com.edufelip.shared.ui.vm.AuthViewModel
import com.edufelip.shared.ui.vm.NoteUiViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize

class MainActivity : ComponentActivity() {
    private val koin by lazy { getSharedKoin() }
    private val vm by lazy { koin.get<NoteUiViewModel>() }
    private val authViewModel by lazy { koin.get<AuthViewModel>() }
    private val settings by lazy { koin.get<Settings>() }
    private val noteDb by lazy { koin.get<NoteDatabase>() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.initialize(applicationContext)

        val appVersion = runCatching {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "0.0.0"
        }.getOrElse { "0.0.0" }

        setContent {
            enableEdgeToEdge()
            AmazingNoteApp(
                viewModel = vm,
                authViewModel = authViewModel,
                googleSignInConfig = GoogleSignInConfig(
                    androidServerClientId = getString(R.string.default_web_client_id),
                ),
                settings = settings,
                noteDatabase = noteDb,
                appVersion = appVersion,
            )
        }
    }
}
