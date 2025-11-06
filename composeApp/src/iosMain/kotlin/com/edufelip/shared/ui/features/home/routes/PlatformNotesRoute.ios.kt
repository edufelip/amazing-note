package com.edufelip.shared.ui.features.home.routes

import androidx.compose.runtime.Composable
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.ui.attachments.AttachmentPicker
import com.edufelip.shared.ui.features.home.screens.HomeScreen
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.vm.AuthViewModel
import com.edufelip.shared.ui.vm.NoteUiViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
actual fun PlatformNotesRoute(
    notes: List<Note>,
    _folders: List<Folder>,
    _trash: List<Note>,
    authViewModel: AuthViewModel,
    _attachmentPicker: AttachmentPicker?,
    viewModel: NoteUiViewModel,
    syncManager: NotesSyncManager,
    coroutineScope: CoroutineScope,
    onNavigate: (AppRoutes) -> Unit,
) {
    HomeScreen(
        notes = notes,
        auth = authViewModel,
        onOpenNote = { note ->
            onNavigate(AppRoutes.NoteDetail(note.id, note.folderId))
        },
        onAdd = { onNavigate(AppRoutes.NoteDetail(null, null)) },
        onDelete = { note ->
            coroutineScope.launch {
                viewModel.setDeleted(note.id, true)
                syncManager.syncLocalToRemoteOnly()
            }
        },
    )
}
