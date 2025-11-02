package com.edufelip.shared.ui.features.home.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.ui.features.home.screens.HomeScreen
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.vm.AuthViewModel
import com.edufelip.shared.ui.vm.NoteUiViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NotesRoute(
    viewModel: NoteUiViewModel,
    authViewModel: AuthViewModel,
    syncManager: NotesSyncManager,
    coroutineScope: CoroutineScope,
    onNavigate: (AppRoutes) -> Unit,
) {
    val notes by viewModel.notes.collectAsState(initial = emptyList())

    HomeScreen(
        notes = notes,
        auth = authViewModel,
        onOpenNote = { note ->
            onNavigate(AppRoutes.NoteDetail(note.id, note.folderId))
        },
        onAdd = {
            onNavigate(AppRoutes.NoteDetail(null, null))
        },
        onDelete = { note ->
            coroutineScope.launch {
                viewModel.setDeleted(note.id, true)
                syncManager.syncLocalToRemoteOnly()
            }
        },
    )
}
