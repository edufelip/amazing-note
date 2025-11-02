package com.edufelip.shared.ui.features.notes.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.domain.model.toLegacyContent
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.unassigned_notes
import com.edufelip.shared.ui.attachments.AttachmentPicker
import com.edufelip.shared.ui.features.notes.screens.FolderDetailScreen
import com.edufelip.shared.ui.features.notes.screens.FoldersScreen
import com.edufelip.shared.ui.features.notes.screens.NoteDetailScreen
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.vm.NoteUiViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun FoldersRoute(
    viewModel: NoteUiViewModel,
    syncManager: NotesSyncManager,
    coroutineScope: CoroutineScope,
    onNavigate: (AppRoutes) -> Unit,
) {
    val folders by viewModel.folders.collectAsState(initial = emptyList())
    val notes by viewModel.notes.collectAsState(initial = emptyList())

    FoldersScreen(
        folders = folders,
        notes = notes,
        onOpenFolder = { folder ->
            onNavigate(AppRoutes.FolderDetail(folder.id))
        },
        onCreateFolder = { name ->
            coroutineScope.launch {
                val trimmed = name.trim()
                if (trimmed.isNotEmpty()) {
                    viewModel.createFolder(trimmed)
                    syncManager.syncLocalToRemoteOnly()
                }
            }
        },
        onRenameFolder = { folder, newName ->
            coroutineScope.launch {
                val trimmed = newName.trim()
                if (trimmed.isNotEmpty() && trimmed != folder.name) {
                    viewModel.renameFolder(folder.id, trimmed)
                    syncManager.syncLocalToRemoteOnly()
                }
            }
        },
        onDeleteFolder = { folder ->
            coroutineScope.launch {
                viewModel.deleteFolder(folder.id)
                syncManager.syncLocalToRemoteOnly()
            }
        },
    )
}

@Composable
fun FolderDetailRoute(
    route: AppRoutes.FolderDetail,
    viewModel: NoteUiViewModel,
    syncManager: NotesSyncManager,
    coroutineScope: CoroutineScope,
    onNavigate: (AppRoutes) -> Unit,
    onBack: () -> Unit,
) {
    val folders by viewModel.folders.collectAsState(initial = emptyList())
    val unassignedNotes by viewModel.notesWithoutFolder.collectAsState(initial = emptyList())
    val folderId = route.id
    val folderTitle = folderId?.let { id ->
        folders.firstOrNull { it.id == id }?.name
    } ?: stringResource(Res.string.unassigned_notes)

    val notesFlow = if (folderId == null) {
        viewModel.notesWithoutFolder
    } else {
        viewModel.notesByFolder(folderId)
    }

    val folderNotes by notesFlow.collectAsState(
        initial = if (folderId == null) unassignedNotes else emptyList(),
    )

    FolderDetailScreen(
        title = folderTitle,
        notes = folderNotes,
        onBack = onBack,
        onOpenNote = { note ->
            onNavigate(AppRoutes.NoteDetail(note.id, note.folderId))
        },
        onAddNote = {
            onNavigate(AppRoutes.NoteDetail(null, folderId))
        },
        onDeleteNote = { note ->
            coroutineScope.launch {
                viewModel.setDeleted(note.id, true)
                syncManager.syncLocalToRemoteOnly()
            }
        },
        onRenameFolder = folderId?.let { id ->
            { newName ->
                coroutineScope.launch {
                    viewModel.renameFolder(id, newName)
                }
            }
        },
        onDeleteFolder = folderId?.let { id ->
            {
                coroutineScope.launch {
                    viewModel.deleteFolder(id)
                    syncManager.syncLocalToRemoteOnly()
                    onBack()
                }
            }
        },
    )
}

@Composable
fun NoteDetailRoute(
    route: AppRoutes.NoteDetail,
    viewModel: NoteUiViewModel,
    syncManager: NotesSyncManager,
    coroutineScope: CoroutineScope,
    attachmentPicker: AttachmentPicker?,
    onBack: () -> Unit,
) {
    val notes by viewModel.notes.collectAsState(initial = emptyList())
    val trash by viewModel.trash.collectAsState(initial = emptyList())
    val folders by viewModel.folders.collectAsState(initial = emptyList())

    val editing = route.id?.let { id ->
        notes.firstOrNull { it.id == id } ?: trash.firstOrNull { it.id == id }
    }
    val initialFolderId = editing?.folderId ?: route.folderId

    NoteDetailScreen(
        id = route.id,
        editing = editing,
        folders = folders,
        initialFolderId = initialFolderId,
        onBack = onBack,
        saveAndValidate = { noteId, title, content, folderId ->
            val legacy = content.toLegacyContent()
            val result = if (noteId == null) {
                viewModel.insert(
                    title = title,
                    description = legacy.description,
                    spans = legacy.spans,
                    attachments = legacy.attachments,
                    folderId = folderId,
                    content = content,
                )
            } else {
                viewModel.update(
                    id = noteId,
                    title = title,
                    description = legacy.description,
                    deleted = false,
                    spans = legacy.spans,
                    attachments = legacy.attachments,
                    folderId = folderId,
                    content = content,
                )
            }
            syncManager.syncLocalToRemoteOnly()
            result
        },
        onDelete = { noteId ->
            coroutineScope.launch {
                viewModel.setDeleted(noteId, true)
                syncManager.syncLocalToRemoteOnly()
            }
        },
        attachmentPicker = attachmentPicker,
    )
}
