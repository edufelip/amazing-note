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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.toLegacyContent
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.select_a_note_to_start
import com.edufelip.shared.ui.attachments.AttachmentPicker
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.features.home.screens.HomeScreen
import com.edufelip.shared.ui.features.notes.screens.NoteDetailScreen
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.vm.AuthViewModel
import com.edufelip.shared.ui.vm.NoteUiViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
    coroutineScope: CoroutineScope,
    onNavigate: (AppRoutes) -> Unit,
    isUserAuthenticated: Boolean,
) {
    val configuration = LocalConfiguration.current
    val isCompactWidth = remember(configuration) {
        configuration.screenWidthDp < 600
    }

    val onDelete: (Note) -> Unit = { note ->
        coroutineScope.launch {
            viewModel.setDeleted(note.id, true)
            if (isUserAuthenticated) {
                syncManager.syncLocalToRemoteOnly()
            }
        }
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
                syncManager = syncManager,
                coroutineScope = coroutineScope,
                attachmentPicker = attachmentPicker,
                onClose = { navigator.navigateBack() },
                isUserAuthenticated = isUserAuthenticated,
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
    syncManager: NotesSyncManager,
    coroutineScope: CoroutineScope,
    attachmentPicker: AttachmentPicker?,
    onClose: () -> Unit,
    isUserAuthenticated: Boolean,
) {
    if (destination == null) {
        NotesDetailPlaceholder()
        return
    }

    val editing = destination.noteId?.let { id ->
        notes.firstOrNull { it.id == id } ?: trash.firstOrNull { it.id == id }
    }

    val initialFolderId = editing?.folderId ?: destination.presetFolderId

    NoteDetailScreen(
        id = destination.noteId,
        editing = editing,
        folders = folders,
        initialFolderId = initialFolderId,
        onBack = onClose,
        isUserAuthenticated = isUserAuthenticated,
        saveAndValidate = { id, title, content, folderId ->
            val legacy = content.toLegacyContent()
            val result = if (id == null) {
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
                    id = id,
                    title = title,
                    description = legacy.description,
                    deleted = false,
                    spans = legacy.spans,
                    attachments = legacy.attachments,
                    folderId = folderId,
                    content = content,
                )
            }
            if (isUserAuthenticated) {
                syncManager.syncLocalToRemoteOnly()
            }
            result
        },
        onDelete = { noteId ->
            coroutineScope.launch {
                viewModel.setDeleted(noteId, true)
                if (isUserAuthenticated) {
                    syncManager.syncLocalToRemoteOnly()
                }
                onClose()
            }
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
