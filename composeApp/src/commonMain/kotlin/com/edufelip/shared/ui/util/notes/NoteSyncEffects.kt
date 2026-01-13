package com.edufelip.shared.ui.util.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.data.network.LocalNetworkStatus
import com.edufelip.shared.ui.vm.NoteUiViewModel
import com.edufelip.shared.ui.vm.NotesEvent
import com.edufelip.shared.ui.util.lifecycle.collectWithLifecycle

@Composable
fun CollectNoteSyncEvents(
    viewModel: NoteUiViewModel,
    syncManager: NotesSyncManager,
    isUserAuthenticated: Boolean,
) {
    val networkStatus = LocalNetworkStatus.current
    val isOnline = networkStatus?.isOnline?.collectWithLifecycle()?.value ?: true
    LaunchedEffect(viewModel.events, isUserAuthenticated, isOnline) {
        if (!isUserAuthenticated || !isOnline) return@LaunchedEffect
        viewModel.events.collect { event ->
            if (event.requiresSync()) {
                syncManager.syncLocalToRemoteOnly()
            }
        }
    }
}

private fun NotesEvent.requiresSync(): Boolean = when (this) {
    is NotesEvent.NoteSaved -> true
    NotesEvent.SyncRequested -> true
    else -> false
}
