package com.edufelip.shared.ui.features.home.routes

import androidx.compose.runtime.Composable
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.ui.attachments.AttachmentPicker
import com.edufelip.shared.ui.features.home.screens.HomeScreen
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.util.notes.CollectNoteSyncEvents
import com.edufelip.shared.ui.vm.AuthViewModel
import com.edufelip.shared.ui.vm.NoteUiViewModel

@Composable
actual fun PlatformNotesRoute(
    notes: List<Note>,
    folders: List<Folder>,
    trash: List<Note>,
    authViewModel: AuthViewModel,
    attachmentPicker: AttachmentPicker?,
    viewModel: NoteUiViewModel,
    syncManager: NotesSyncManager,
    onNavigate: (AppRoutes) -> Unit,
    isUserAuthenticated: Boolean,
    onAvatarClick: () -> Unit,
    onLogout: () -> Unit,
) {
    CollectNoteSyncEvents(
        viewModel = viewModel,
        syncManager = syncManager,
        isUserAuthenticated = isUserAuthenticated,
    )
    HomeScreen(
        notes = notes,
        auth = authViewModel,
        onOpenNote = { note ->
            onNavigate(AppRoutes.NoteDetail(note.id, note.folderId))
        },
        onAdd = { onNavigate(AppRoutes.NoteDetail(null, null)) },
        onAvatarClick = onAvatarClick,
        onLogout = onLogout,
    )
}
