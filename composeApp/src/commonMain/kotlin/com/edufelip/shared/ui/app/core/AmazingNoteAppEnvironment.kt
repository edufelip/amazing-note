package com.edufelip.shared.ui.app.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.edufelip.shared.data.auth.AuthService
import com.edufelip.shared.data.auth.GoogleSignInConfig
import com.edufelip.shared.data.auth.GoogleSignInLauncher
import com.edufelip.shared.data.auth.rememberGoogleSignInLauncher
import com.edufelip.shared.data.db.DatabaseDriverFactory
import com.edufelip.shared.data.db.createDatabase
import com.edufelip.shared.data.repository.DefaultAuthRepository
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.domain.repository.AuthRepository
import com.edufelip.shared.domain.usecase.AuthUseCases
import com.edufelip.shared.domain.usecase.buildAuthUseCases
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
    val authRepository: AuthRepository,
    val authUseCases: AuthUseCases,
    val noteDatabase: NoteDatabase,
    val notesSyncManager: NotesSyncManager,
    val attachmentPicker: AttachmentPicker?,
    val googleSignInLauncher: GoogleSignInLauncher?,
)

@Composable
fun rememberAmazingNoteAppEnvironment(
    authService: AuthService,
    googleSignInConfig: GoogleSignInConfig,
    settings: Settings,
    appPreferences: AppPreferences = DefaultAppPreferences(settings),
    scope: CoroutineScope,
    noteDatabaseProvider: () -> NoteDatabase = { createDatabase(DatabaseDriverFactory()) },
    syncManagerProvider: (NoteDatabase, CoroutineScope) -> NotesSyncManager = { database, coroutineScope ->
        NotesSyncManager(database, coroutineScope)
    },
    authRepositoryFactory: (AuthService) -> AuthRepository = { service ->
        DefaultAuthRepository(service)
    },
    authUseCasesFactory: (AuthRepository) -> AuthUseCases = { repository ->
        buildAuthUseCases(repository)
    },
    attachmentPickerProvider: @Composable () -> AttachmentPicker? = { rememberAttachmentPicker() },
    googleSignInLauncherProvider: @Composable (GoogleSignInConfig) -> GoogleSignInLauncher? = {
        rememberGoogleSignInLauncher(it)
    },
    noteDatabase: NoteDatabase? = null,
    notesSyncManager: NotesSyncManager? = null,
): AmazingNoteAppEnvironment {
    val resolvedSettings = remember(settings) { settings }
    val resolvedPreferences = remember(appPreferences) { appPreferences }

    val resolvedDatabase = remember(noteDatabase, noteDatabaseProvider) {
        noteDatabase ?: noteDatabaseProvider()
    }
    val resolvedRepository = remember(authService, authRepositoryFactory) {
        authRepositoryFactory(authService)
    }
    val resolvedUseCases = remember(resolvedRepository, authUseCasesFactory) {
        authUseCasesFactory(resolvedRepository)
    }
    val resolvedSyncManager = remember(notesSyncManager, resolvedDatabase, syncManagerProvider, scope) {
        notesSyncManager ?: syncManagerProvider(resolvedDatabase, scope)
    }

    val resolvedAttachmentPicker = attachmentPickerProvider()
    val resolvedGoogleSignInLauncher = googleSignInLauncherProvider(googleSignInConfig)

    return AmazingNoteAppEnvironment(
        settings = resolvedSettings,
        appPreferences = resolvedPreferences,
        authRepository = resolvedRepository,
        authUseCases = resolvedUseCases,
        noteDatabase = resolvedDatabase,
        notesSyncManager = resolvedSyncManager,
        attachmentPicker = resolvedAttachmentPicker,
        googleSignInLauncher = resolvedGoogleSignInLauncher,
    )
}
