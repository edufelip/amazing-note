package com.edufelip.shared.ui.util.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.ui.vm.NoteUiViewModel
import com.edufelip.shared.ui.vm.NotesEvent

@Composable
fun CollectNoteSyncEvents(
    viewModel: NoteUiViewModel,
    syncManager: NotesSyncManager,
    isUserAuthenticated: Boolean,
) {
    LaunchedEffect(viewModel.events, isUserAuthenticated) {
        if (!isUserAuthenticated) return@LaunchedEffect
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
