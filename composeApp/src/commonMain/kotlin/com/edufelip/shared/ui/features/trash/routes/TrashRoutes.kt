package com.edufelip.shared.ui.features.trash.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.ui.features.trash.screens.TrashScreen
import com.edufelip.shared.ui.util.lifecycle.collectWithLifecycle
import com.edufelip.shared.ui.util.notes.CollectNoteSyncEvents
import com.edufelip.shared.ui.vm.NoteUiViewModel

@Composable
fun TrashRoute(
    viewModel: NoteUiViewModel,
    syncManager: NotesSyncManager,
    onBack: () -> Unit,
    isUserAuthenticated: Boolean,
) {
    CollectNoteSyncEvents(
        viewModel = viewModel,
        syncManager = syncManager,
        isUserAuthenticated = isUserAuthenticated,
    )
    val notesState by viewModel.state.collectWithLifecycle()
    val trash = notesState.trash

    fun deleteNotes(notes: List<Note>) {
        if (notes.isEmpty()) return
        val lastIndex = notes.lastIndex
        notes.forEachIndexed { index, note ->
            viewModel.delete(
                id = note.id,
                syncAfter = isUserAuthenticated && index == lastIndex,
            )
        }
    }

    TrashScreen(
        notes = trash,
        onRestore = { note ->
            viewModel.setDeleted(
                id = note.id,
                deleted = false,
                syncAfter = isUserAuthenticated,
            )
        },
        onDeleteNotes = { notes -> deleteNotes(notes) },
        onBack = onBack,
        onEmptyTrash = { deleteNotes(trash) },
    )
}
