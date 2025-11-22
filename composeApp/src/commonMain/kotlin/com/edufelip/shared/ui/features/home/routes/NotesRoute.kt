package com.edufelip.shared.ui.features.home.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.ui.attachments.AttachmentPicker
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.util.lifecycle.collectWithLifecycle
import com.edufelip.shared.ui.vm.AuthViewModel
import com.edufelip.shared.ui.vm.NoteUiViewModel
@Composable
fun NotesRoute(
    viewModel: NoteUiViewModel,
    authViewModel: AuthViewModel,
    syncManager: NotesSyncManager,
    onNavigate: (AppRoutes) -> Unit,
    attachmentPicker: AttachmentPicker?,
    isUserAuthenticated: Boolean,
) {
    val notesState by viewModel.state.collectWithLifecycle()
    val notes = notesState.notes
    val folders = notesState.folders
    val trash = notesState.trash

    PlatformNotesRoute(
        notes = notes,
        folders = folders,
        trash = trash,
        authViewModel = authViewModel,
        attachmentPicker = attachmentPicker,
        viewModel = viewModel,
        syncManager = syncManager,
        onNavigate = onNavigate,
        isUserAuthenticated = isUserAuthenticated,
    )
}

@Composable
expect fun PlatformNotesRoute(
    notes: List<Note>,
    folders: List<Folder>,
    trash: List<Note>,
    authViewModel: AuthViewModel,
    attachmentPicker: AttachmentPicker?,
    viewModel: NoteUiViewModel,
    syncManager: NotesSyncManager,
    onNavigate: (AppRoutes) -> Unit,
    isUserAuthenticated: Boolean,
)
