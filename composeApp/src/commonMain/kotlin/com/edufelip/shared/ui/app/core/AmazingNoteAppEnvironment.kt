package com.edufelip.shared.ui.app.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.edufelip.shared.data.auth.AppleSignInLauncher
import com.edufelip.shared.data.auth.GoogleSignInConfig
import com.edufelip.shared.data.auth.GoogleSignInLauncher
import com.edufelip.shared.data.auth.rememberAppleSignInLauncher
import com.edufelip.shared.data.auth.rememberGoogleSignInLauncher
import com.edufelip.shared.data.db.DatabaseDriverFactory
import com.edufelip.shared.data.db.createDatabase
import com.edufelip.shared.data.network.NetworkStatus
import com.edufelip.shared.data.network.rememberNetworkStatus
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.ui.attachments.AttachmentPicker
import com.edufelip.shared.ui.attachments.rememberAttachmentPicker
import com.edufelip.shared.ui.settings.AppPreferences
import com.edufelip.shared.ui.settings.DefaultAppPreferences
import com.edufelip.shared.ui.settings.Settings
import kotlinx.coroutines.CoroutineScope

/**
 * Aggregates all runtime dependencies required by [AmazingNoteApp].
 */
data class AmazingNoteAppEnvironment(
    val settings: Settings,
    val appPreferences: AppPreferences,
    val noteDatabase: NoteDatabase,
    val notesSyncManager: NotesSyncManager,
    val networkStatus: NetworkStatus,
    val attachmentPicker: AttachmentPicker?,
    val googleSignInLauncher: GoogleSignInLauncher?,
    val appleSignInLauncher: AppleSignInLauncher?,
)

@Composable
fun rememberAmazingNoteAppEnvironment(
    googleSignInConfig: GoogleSignInConfig,
    settings: Settings,
    appPreferences: AppPreferences = DefaultAppPreferences(settings),
    scope: CoroutineScope,
    noteDatabaseProvider: () -> NoteDatabase = { createDatabase(DatabaseDriverFactory()) },
    syncManagerProvider: (NoteDatabase, CoroutineScope) -> NotesSyncManager = { database, coroutineScope ->
        NotesSyncManager(database, coroutineScope)
    },
    networkStatusProvider: @Composable () -> NetworkStatus = { rememberNetworkStatus() },
    attachmentPickerProvider: @Composable () -> AttachmentPicker? = { rememberAttachmentPicker() },
    googleSignInLauncherProvider: @Composable (GoogleSignInConfig) -> GoogleSignInLauncher? = {
        rememberGoogleSignInLauncher(it)
    },
    appleSignInLauncherProvider: @Composable () -> AppleSignInLauncher? = { rememberAppleSignInLauncher() },
    noteDatabase: NoteDatabase? = null,
    notesSyncManager: NotesSyncManager? = null,
    networkStatus: NetworkStatus? = null,
): AmazingNoteAppEnvironment {
    val resolvedSettings = remember(settings) { settings }
    val resolvedPreferences = remember(appPreferences) { appPreferences }

    val resolvedDatabase = remember(noteDatabase, noteDatabaseProvider) {
        noteDatabase ?: noteDatabaseProvider()
    }
    val resolvedSyncManager = remember(notesSyncManager, resolvedDatabase, syncManagerProvider, scope) {
        notesSyncManager ?: syncManagerProvider(resolvedDatabase, scope)
    }

    val resolvedNetworkStatus = networkStatus ?: networkStatusProvider()

    val resolvedAttachmentPicker = attachmentPickerProvider()
    val resolvedGoogleSignInLauncher = googleSignInLauncherProvider(googleSignInConfig)
    val resolvedAppleSignInLauncher = appleSignInLauncherProvider()

    return remember(
        resolvedSettings,
        resolvedPreferences,
        resolvedDatabase,
        resolvedSyncManager,
        resolvedNetworkStatus,
        resolvedAttachmentPicker,
        resolvedGoogleSignInLauncher,
        resolvedAppleSignInLauncher,
    ) {
        AmazingNoteAppEnvironment(
            settings = resolvedSettings,
            appPreferences = resolvedPreferences,
            noteDatabase = resolvedDatabase,
            notesSyncManager = resolvedSyncManager,
            networkStatus = resolvedNetworkStatus,
            attachmentPicker = resolvedAttachmentPicker,
            googleSignInLauncher = resolvedGoogleSignInLauncher,
            appleSignInLauncher = resolvedAppleSignInLauncher,
        )
    }
}
