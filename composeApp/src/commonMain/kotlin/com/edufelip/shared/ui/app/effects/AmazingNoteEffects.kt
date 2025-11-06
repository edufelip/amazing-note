package com.edufelip.shared.ui.app.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.edufelip.shared.data.auth.AuthUser
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.ui.app.state.AmazingNoteAppState
import kotlinx.coroutines.delay

@Composable
fun ScheduleInitialSync(syncManager: NotesSyncManager) {
    LaunchedEffect(syncManager) {
        syncManager.start()
    }
}

@Composable
fun SyncOnUserChange(user: AuthUser?, syncManager: NotesSyncManager) {
    LaunchedEffect(syncManager, user?.uid) {
        val uid = user?.uid ?: return@LaunchedEffect
        syncManager.syncNow()
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
