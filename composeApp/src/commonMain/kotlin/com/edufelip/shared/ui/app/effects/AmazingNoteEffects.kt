package com.edufelip.shared.ui.app.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.data.sync.SyncEvent
import com.edufelip.shared.ui.app.state.AmazingNoteAppState
import com.edufelip.shared.ui.effects.toast.rememberToastController
import com.edufelip.shared.ui.effects.toast.show
import com.edufelip.shared.ui.util.lifecycle.collectWithLifecycle
import kotlinx.coroutines.delay

@Composable
fun ScheduleInitialSync(syncManager: NotesSyncManager) {
    LaunchedEffect(syncManager) {
        syncManager.start()
    }
}

@Composable
fun SyncOnUserChange(state: AmazingNoteAppState, syncManager: NotesSyncManager) {
    val authUiState by state.authViewModel.uiState.collectWithLifecycle()
    val user = authUiState.user
    LaunchedEffect(syncManager, user?.uid, authUiState.isUserResolved) {
        if (!authUiState.isUserResolved) return@LaunchedEffect
        val uid = user?.uid ?: return@LaunchedEffect
        syncManager.syncNow(uid)
    }
}

@Composable
fun BottomBarVisibilityEffect(state: AmazingNoteAppState) {
    LaunchedEffect(state.isBottomBarEnabled, state.bottomBarTargetVisible) {
        if (state.isBottomBarEnabled) {
            if (state.bottomBarTargetVisible) {
                delay(100)
            }
            state.setBottomBarVisibility(state.bottomBarTargetVisible)
        } else {
            state.setBottomBarVisibility(false)
        }
    }
}

@Composable
fun PlatformTabBarVisibilityEffect(
    state: AmazingNoteAppState,
    onVisibilityChanged: (Boolean) -> Unit,
) {
    val isTabVisible = state.isTab(state.currentRoute)
    LaunchedEffect(isTabVisible) {
        onVisibilityChanged(isTabVisible)
    }
}

@Composable
fun SyncEventNotifications(syncManager: NotesSyncManager) {
    val toastController = rememberToastController()
    LaunchedEffect(syncManager) {
        syncManager.events.collect { event ->
            if (event is SyncEvent.SyncFailed) {
                toastController.show(event.message)
            }
        }
    }
}
