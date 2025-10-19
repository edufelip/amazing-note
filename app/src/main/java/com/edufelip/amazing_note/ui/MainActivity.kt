package com.edufelip.amazing_note.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.edufelip.amazing_note.ui.viewmodels.KmpNoteViewModel
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.ui.AmazingNoteApp
import com.edufelip.shared.ui.settings.Settings
import com.edufelip.shared.auth.GitLiveAuthService
import com.edufelip.shared.auth.GoogleSignInConfig
import com.edufelip.amazing_note.R
import dagger.hilt.android.AndroidEntryPoint
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val vm: KmpNoteViewModel by viewModels()
    private val authService by lazy { GitLiveAuthService() }

    @Inject lateinit var settings: Settings

    @Inject lateinit var noteDb: NoteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.initialize(applicationContext)

        enableEdgeToEdge()

        val appVersion = runCatching {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "0.0.0"
        }.getOrElse { "0.0.0" }

        setContent {
            AmazingNoteApp(
                viewModel = vm,
                authService = authService,
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
