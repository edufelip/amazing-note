package com.edufelip.shared.ui.features.settings.routes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edufelip.shared.ui.app.state.AmazingNoteAppState
import com.edufelip.shared.ui.features.settings.screens.PrivacyScreen
import com.edufelip.shared.ui.features.settings.screens.SettingsScreen
import com.edufelip.shared.ui.nav.AppRoutes
import kotlinx.coroutines.launch

@Composable
fun SettingsRoute(
    state: AmazingNoteAppState,
    darkTheme: Boolean,
    appVersion: String,
    onNavigate: (AppRoutes) -> Unit,
) {
    val authViewModel = state.authViewModel
    val scope = state.coroutineScope
    val syncManager = state.environment.notesSyncManager

    SettingsScreen(
        darkTheme = darkTheme,
        onToggleDarkTheme = { enabled -> state.toggleTheme(enabled) },
        auth = authViewModel,
        onLogin = { onNavigate(AppRoutes.Login) },
        onLogout = {
            scope.launch {
                if (authViewModel.uiState.value.user != null) {
                    runCatching { syncManager.syncLocalToRemoteOnly() }
                }
                authViewModel.logout()
            }
        },
        onOpenTrash = { onNavigate(AppRoutes.Trash) },
        onOpenPrivacy = { onNavigate(AppRoutes.Privacy) },
        appVersion = appVersion,
    )
}

@Composable
fun PrivacyRoute(
    onBack: () -> Unit,
) {
    PrivacyScreen(
        modifier = Modifier.fillMaxSize(),
        onBack = onBack,
    )
}
