package com.edufelip.shared.ui.features.notes.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.domain.validation.NoteValidationRules
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.unassigned_notes
import com.edufelip.shared.ui.attachments.AttachmentPicker
import com.edufelip.shared.ui.features.notes.screens.FolderDetailScreen
import com.edufelip.shared.ui.features.notes.screens.FoldersScreen
import com.edufelip.shared.ui.features.notes.screens.NoteDetailScreen
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.settings.LocalAppPreferences
import com.edufelip.shared.ui.util.lifecycle.collectWithLifecycle
import com.edufelip.shared.ui.util.notes.CollectNoteSyncEvents
import com.edufelip.shared.ui.util.security.SecurityLogger
import com.edufelip.shared.ui.util.security.sanitizeInlineInput
import com.edufelip.shared.ui.vm.AuthViewModel
import com.edufelip.shared.ui.vm.NoteUiViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun FoldersRoute(
    viewModel: NoteUiViewModel,
    syncManager: NotesSyncManager,
    onNavigate: (AppRoutes) -> Unit,
    isDarkTheme: Boolean,
    authViewModel: AuthViewModel,
    isUserAuthenticated: Boolean,
    onAvatarClick: () -> Unit,
    onLogout: () -> Unit,
) {
    CollectNoteSyncEvents(
        viewModel = viewModel,
        syncManager = syncManager,
        isUserAuthenticated = isUserAuthenticated,
    )
    val notesState by viewModel.state.collectWithLifecycle()
    val folders = notesState.folders
    val notes = notesState.notes
    val appPreferences = LocalAppPreferences.current
    val layoutMode by appPreferences.folderLayoutFlow.collectWithLifecycle(initial = appPreferences.folderLayout())

    FoldersScreen(
        folders = folders,
        notes = notes,
        isDarkTheme = isDarkTheme,
        auth = authViewModel,
        layoutMode = layoutMode,
        onLayoutChange = { appPreferences.setFolderLayout(it) },
        onOpenFolder = { folder ->
            onNavigate(AppRoutes.FolderDetail(folder.id))
        },
        onAvatarClick = onAvatarClick,
        onCreateFolder = { name ->
            val sanitized = sanitizeInlineInput(name, maxLength = 50)
            if (sanitized.modified) {
                SecurityLogger.logSanitized(flow = "folders", field = "create", rawSample = name)
            }
            if (sanitized.value.isNotEmpty()) {
                viewModel.createFolder(
                    name = sanitized.value,
                    syncAfter = isUserAuthenticated,
                )
            }
        },
        onRenameFolder = { folder, newName ->
            val sanitized = sanitizeInlineInput(newName, maxLength = 50)
            if (sanitized.modified) {
                SecurityLogger.logSanitized(flow = "folders", field = "rename", rawSample = newName)
            }
            if (sanitized.value.isNotEmpty() && sanitized.value != folder.name) {
                viewModel.renameFolder(
                    id = folder.id,
                    name = sanitized.value,
                    syncAfter = isUserAuthenticated,
                )
            }
        },
        onDeleteFolder = { folder ->
            viewModel.deleteFolder(folder.id, syncAfter = isUserAuthenticated)
        },
        onLogout = onLogout,
    )
}

@Composable
fun FolderDetailRoute(
    route: AppRoutes.FolderDetail,
    viewModel: NoteUiViewModel,
    syncManager: NotesSyncManager,
    onNavigate: (AppRoutes) -> Unit,
    onBack: () -> Unit,
    isUserAuthenticated: Boolean,
) {
    CollectNoteSyncEvents(
        viewModel = viewModel,
        syncManager = syncManager,
        isUserAuthenticated = isUserAuthenticated,
    )
    val notesState by viewModel.state.collectWithLifecycle()
    val folders = notesState.folders
    val unassignedNotes = notesState.notesWithoutFolder
    val folderId = route.id
    val folderTitle = folderId?.let { id ->
        folders.firstOrNull { it.id == id }?.name
    } ?: stringResource(Res.string.unassigned_notes)

    val folderNotes = if (folderId == null) {
        unassignedNotes
    } else {
        notesState.notes.filter { it.folderId == folderId }
    }

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
        onRenameFolder = folderId?.let { id ->
            { newName ->
                val sanitized = sanitizeInlineInput(newName, maxLength = 50)
                if (sanitized.modified) {
                    SecurityLogger.logSanitized(flow = "folders_detail", field = "rename", rawSample = newName)
                }
                if (sanitized.value.isNotEmpty()) {
                    viewModel.renameFolder(
                        id = id,
                        name = sanitized.value,
                        syncAfter = isUserAuthenticated,
                    )
                }
            }
        },
        onDeleteFolder = folderId?.let { id ->
            {
                viewModel.deleteFolder(id, syncAfter = isUserAuthenticated)
                onBack()
            }
        },
    )
}

@Composable
fun NoteDetailRoute(
    route: AppRoutes.NoteDetail,
    viewModel: NoteUiViewModel,
    syncManager: NotesSyncManager,
    attachmentPicker: AttachmentPicker?,
    onBack: () -> Unit,
    isUserAuthenticated: Boolean,
    currentUserId: String? = null,
) {
    CollectNoteSyncEvents(
        viewModel = viewModel,
        syncManager = syncManager,
        isUserAuthenticated = isUserAuthenticated,
    )
    val notesState by viewModel.state.collectWithLifecycle()
    val notes = notesState.notes
    val trash = notesState.trash
    val folders = notesState.folders
    val noteValidationRules = NoteValidationRules()

    val editing = route.id?.let { id ->
        notes.firstOrNull { it.id == id } ?: trash.firstOrNull { it.id == id }
    }
    val initialFolderId = editing?.folderId ?: route.folderId

    NoteDetailScreen(
        id = route.id,
        editing = editing,
        folders = folders,
        initialFolderId = initialFolderId,
        noteValidationRules = noteValidationRules,
        onBack = onBack,
        isUserAuthenticated = isUserAuthenticated,
        currentUserId = currentUserId,
        onSaveNote = { noteId, title, description, spans, attachments, folderId, content, stableId, navigateBack ->
            if (noteId == null) {
                viewModel.insert(
                    title = title,
                    description = description,
                    spans = spans,
                    attachments = attachments,
                    folderId = folderId,
                    content = content,
                    stableId = stableId,
                    navigateBack = navigateBack,
                    cleanupAttachments = isUserAuthenticated,
                )
            } else {
                viewModel.update(
                    id = noteId,
                    title = title,
                    description = description,
                    deleted = false,
                    spans = spans,
                    attachments = attachments,
                    folderId = folderId,
                    content = content,
                    navigateBack = navigateBack,
                    cleanupAttachments = isUserAuthenticated,
                )
            }
        },
        events = viewModel.events,
        onDelete = { noteId ->
            viewModel.setDeleted(
                id = noteId,
                deleted = true,
                syncAfter = isUserAuthenticated,
            )
        },
        attachmentPicker = attachmentPicker,
    )
}
