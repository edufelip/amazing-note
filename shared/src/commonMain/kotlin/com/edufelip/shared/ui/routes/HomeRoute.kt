package com.edufelip.shared.ui.routes

import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.edufelip.shared.model.Note
import com.edufelip.shared.ui.gadgets.DrawerContent
import com.edufelip.shared.ui.screens.ListScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    notes: List<Note>,
    drawerState: DrawerState,
    darkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onOpenTrash: () -> Unit,
    onOpenNote: (Note) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Note) -> Unit
) {
    var query = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val closeDrawer = { scope.launch { drawerState.close() } }

    ListScreen(
        notes = if (query.value.isBlank()) notes else notes.filter {
            it.title.contains(query.value, ignoreCase = true) || it.description.contains(query.value, ignoreCase = true)
        },
        onNoteClick = onOpenNote,
        onAddClick = onAdd,
        searchQuery = query.value,
        onSearchQueryChange = { query.value = it },
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

