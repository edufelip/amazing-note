package com.edufelip.shared.ui

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.edufelip.shared.presentation.NoteUiViewModel
import com.edufelip.shared.domain.validation.NoteActionResult
import com.edufelip.shared.domain.validation.NoteValidationError.DescriptionTooLong
import com.edufelip.shared.domain.validation.NoteValidationError.EmptyDescription
import com.edufelip.shared.domain.validation.NoteValidationError.EmptyTitle
import com.edufelip.shared.domain.validation.NoteValidationError.TitleTooLong
import com.edufelip.shared.i18n.Str
import com.edufelip.shared.i18n.string
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import com.edufelip.shared.ui.gadgets.DrawerContent
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.screens.AddNoteScreen
import com.edufelip.shared.ui.screens.ListScreen
import com.edufelip.shared.ui.screens.TrashScreen
import com.edufelip.shared.ui.theme.AmazingNoteTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmazingNoteApp(viewModel: NoteUiViewModel) {
    var darkTheme by rememberSaveable { mutableStateOf(false) }
    val backStack = remember { mutableStateListOf<AppRoutes>(AppRoutes.Home) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val notes by viewModel.notes.collectAsState(initial = emptyList())
    val trash by viewModel.trash.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    AmazingNoteTheme(darkTheme = darkTheme) {
        when (val current = backStack.last()) {
            is AppRoutes.Home -> HomeRoute(
                notes = notes,
                drawerState = drawerState,
                darkTheme = darkTheme,
                onToggleDarkTheme = { darkTheme = it },
                onOpenTrash = { backStack.add(AppRoutes.Trash) },
                onOpenNote = { note -> backStack.add(AppRoutes.NoteDetail(note.id)) },
                onAdd = { backStack.add(AppRoutes.NoteDetail(null)) },
                onDelete = { note -> scope.launch { viewModel.setDeleted(note.id, true) } }
            )
            is AppRoutes.NoteDetail -> {
                val editing = current.id?.let { id ->
                    notes.firstOrNull { it.id == id } ?: trash.firstOrNull { it.id == id }
                }
                NoteDetailRoute(
                id = current.id,
                editing = editing,
                onBack = { backStack.removeLastOrNull() },
                saveAndValidate = { id, title, priority, description ->
                    if (id == null) viewModel.insert(title, priority, description)
                    else viewModel.update(id, title, priority, description, false)
                },
                onDelete = { idToDelete ->
                    scope.launch { viewModel.setDeleted(idToDelete, true) }
                }
            ) }
            is AppRoutes.Trash -> TrashRoute(
                notes = trash,
                onBack = { backStack.removeLastOrNull() },
                onRestore = { note ->
                    scope.launch { viewModel.setDeleted(note.id, false) }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeRoute(
    notes: List<Note>,
    drawerState: DrawerState,
    darkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onOpenTrash: () -> Unit,
    onOpenNote: (Note) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Note) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val closeDrawer = { scope.launch { drawerState.close() } }

    ListScreen(
        notes = if (query.isBlank()) notes else notes.filter {
            it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
        },
        onNoteClick = onOpenNote,
        onAddClick = onAdd,
        searchQuery = query,
        onSearchQueryChange = { query = it },
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onYourNotesClick = { closeDrawer() },
                onTrashClick = { onOpenTrash(); closeDrawer() },
                darkTheme = darkTheme,
                onToggleDarkTheme = onToggleDarkTheme,
                selectedHome = true,
                selectedTrash = false,
                onPrivacyClick = null
            )
        },
        onDelete = onDelete
    )
}

@Composable
private fun NoteDetailRoute(
    id: Int?,
    editing: Note?,
    onBack: () -> Unit,
    saveAndValidate: suspend (id: Int?, title: String, priority: Priority, description: String) -> NoteActionResult,
    onDelete: (id: Int) -> Unit
) {
    var title by remember { mutableStateOf(editing?.title ?: "") }
    var description by remember { mutableStateOf(editing?.description ?: "") }
    var priority by remember { mutableStateOf(editing?.priority ?: Priority.LOW) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }

    AddNoteScreen(
        title = title,
        onTitleChange = { title = it; titleError = null },
        priority = priority,
        onPriorityChange = { priority = it },
        description = description,
        onDescriptionChange = { description = it; descriptionError = null },
        onBack = onBack,
        onSave = {
            val result = kotlinx.coroutines.runBlocking {
                saveAndValidate(id, title, priority, description)
            }
            when (result) {
                is NoteActionResult.Success -> onBack()
                is NoteActionResult.Invalid -> {
                    titleError = result.errors.firstOrNull {
                        it is EmptyTitle ||
                        it is TitleTooLong
                    }?.let {
                        when (it) {
                            is EmptyTitle -> string(Str.ErrorTitleRequired)
                            is TitleTooLong -> string(Str.ErrorTitleTooLong, it.max)
                            else -> null
                        }
                    }
                    descriptionError = result.errors.firstOrNull {
                        it is EmptyDescription ||
                        it is DescriptionTooLong
                    }?.let {
                        when (it) {
                            is EmptyDescription -> string(Str.ErrorDescriptionRequired)
                            is DescriptionTooLong -> string(Str.ErrorDescriptionTooLong, it.max)
                            else -> null
                        }
                    }
                }
            }
        },
        onDelete = id?.let { noteId -> { onDelete(noteId); onBack() } },
        titleError = titleError,
        descriptionError = descriptionError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrashRoute(
    notes: List<Note>,
    onBack: () -> Unit,
    onRestore: (Note) -> Unit
) {
    TrashScreen(
        notes = notes,
        onBack = onBack,
        onRestore = onRestore
    )
}
