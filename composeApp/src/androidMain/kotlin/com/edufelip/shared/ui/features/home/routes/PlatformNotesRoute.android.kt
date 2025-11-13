package com.edufelip.shared.ui.features.home.routes

import android.os.Parcelable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.validation.NoteValidationRules
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.select_a_note_to_start
import com.edufelip.shared.ui.attachments.AttachmentPicker
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.features.home.screens.HomeScreen
import com.edufelip.shared.ui.features.notes.screens.NoteDetailScreen
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.util.lifecycle.collectWithLifecycle
import com.edufelip.shared.ui.util.notes.CollectNoteSyncEvents
import com.edufelip.shared.ui.vm.AuthViewModel
import com.edufelip.shared.ui.vm.NoteUiViewModel
import kotlinx.parcelize.Parcelize
import org.jetbrains.compose.resources.stringResource

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3AdaptiveApi::class,
)
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
) {
    CollectNoteSyncEvents(
        viewModel = viewModel,
        syncManager = syncManager,
        isUserAuthenticated = isUserAuthenticated,
    )
    val authState by authViewModel.uiState.collectWithLifecycle()
    val currentUserId = authState.user?.uid
    val configuration = LocalConfiguration.current
    val isCompactWidth = remember(configuration) {
        configuration.screenWidthDp < 600
    }

    val onDelete: (Note) -> Unit = { note ->
        viewModel.setDeleted(
            id = note.id,
            deleted = true,
            syncAfter = isUserAuthenticated,
        )
    }

    if (isCompactWidth) {
        HomeScreen(
            notes = notes,
            auth = authViewModel,
            onOpenNote = { note ->
                onNavigate(AppRoutes.NoteDetail(note.id, note.folderId))
            },
            onAdd = { onNavigate(AppRoutes.NoteDetail(null, null)) },
            onDelete = onDelete,
        )
        return
    }

    val navigator = rememberListDetailPaneScaffoldNavigator<NotesPaneDestination>()

    ListDetailPaneScaffold(
        modifier = Modifier.fillMaxSize(),
        scaffoldState = navigator.scaffoldState,
        listPane = {
            NotesListPane(
                notes = notes,
                authViewModel = authViewModel,
                onNavigateToDetail = { destination ->
                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, destination)
                },
                onCreateNote = {
                    navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, NotesPaneDestination())
                },
                onDelete = onDelete,
            )
        },
        detailPane = {
            val destination = navigator.currentDestination?.content as? NotesPaneDestination
            NotesDetailPane(
                destination = destination,
                notes = notes,
                trash = trash,
                folders = folders,
                viewModel = viewModel,
                attachmentPicker = attachmentPicker,
                onClose = { navigator.navigateBack() },
                isUserAuthenticated = isUserAuthenticated,
                currentUserId = currentUserId,
            )
        },
    )
}

@Composable
private fun NotesListPane(
    notes: List<Note>,
    authViewModel: AuthViewModel,
    onNavigateToDetail: (NotesPaneDestination) -> Unit,
    onCreateNote: () -> Unit,
    onDelete: (Note) -> Unit,
) {
    HomeScreen(
        notes = notes,
        auth = authViewModel,
        onOpenNote = { note ->
            onNavigateToDetail(
                NotesPaneDestination(
                    noteId = note.id,
                    presetFolderId = note.folderId,
                ),
            )
        },
        onAdd = onCreateNote,
        onDelete = onDelete,
    )
}

@Composable
private fun NotesDetailPane(
    destination: NotesPaneDestination?,
    notes: List<Note>,
    trash: List<Note>,
    folders: List<Folder>,
    viewModel: NoteUiViewModel,
    attachmentPicker: AttachmentPicker?,
    onClose: () -> Unit,
    isUserAuthenticated: Boolean,
    currentUserId: String?,
) {
    if (destination == null) {
        NotesDetailPlaceholder()
        return
    }

    val editing = destination.noteId?.let { id ->
        notes.firstOrNull { it.id == id } ?: trash.firstOrNull { it.id == id }
    }

    val initialFolderId = editing?.folderId ?: destination.presetFolderId

    val noteValidationRules = NoteValidationRules()

    NoteDetailScreen(
        id = destination.noteId,
        editing = editing,
        folders = folders,
        initialFolderId = initialFolderId,
        noteValidationRules = noteValidationRules,
        onBack = onClose,
        isUserAuthenticated = isUserAuthenticated,
        currentUserId = currentUserId,
        events = viewModel.events,
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
        onDelete = { noteId ->
            viewModel.setDeleted(
                id = noteId,
                deleted = true,
                syncAfter = isUserAuthenticated,
            )
            onClose()
        },
        attachmentPicker = attachmentPicker,
    )
}

@Composable
private fun NotesDetailPlaceholder() {
    val tokens = designTokens()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = tokens.spacing.xl, vertical = tokens.spacing.xxl),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.select_a_note_to_start),
            style = MaterialTheme.typography.titleMedium,
            color = tokens.colors.muted,
            textAlign = TextAlign.Center,
        )
    }
}

@Parcelize
private data class NotesPaneDestination(
    val noteId: Int? = null,
    val presetFolderId: Long? = null,
) : Parcelable
