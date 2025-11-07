package com.edufelip.shared.ui.features.home.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.ui.attachments.AttachmentPicker
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.vm.AuthViewModel
import com.edufelip.shared.ui.vm.NoteUiViewModel
import kotlinx.coroutines.CoroutineScope

@Composable
fun NotesRoute(
    viewModel: NoteUiViewModel,
    authViewModel: AuthViewModel,
    syncManager: NotesSyncManager,
    coroutineScope: CoroutineScope,
    onNavigate: (AppRoutes) -> Unit,
    attachmentPicker: AttachmentPicker?,
    isUserAuthenticated: Boolean,
) {
    val notes by viewModel.notes.collectAsState(initial = emptyList())

    val folders by viewModel.folders.collectAsState(initial = emptyList())
    val trash by viewModel.trash.collectAsState(initial = emptyList())

    PlatformNotesRoute(
        notes = notes,
        folders = folders,
        trash = trash,
        authViewModel = authViewModel,
        attachmentPicker = attachmentPicker,
        viewModel = viewModel,
        syncManager = syncManager,
        coroutineScope = coroutineScope,
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
    coroutineScope: CoroutineScope,
    onNavigate: (AppRoutes) -> Unit,
    isUserAuthenticated: Boolean,
)
