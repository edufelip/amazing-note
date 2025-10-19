package com.edufelip.shared.ui.nav.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.edufelip.shared.model.Note
import com.edufelip.shared.presentation.AuthViewModel
import com.edufelip.shared.sync.LocalNotesSyncManager
import com.edufelip.shared.sync.SyncEvent
import com.edufelip.shared.ui.nav.components.NotesEmptyState
import kotlinx.coroutines.flow.collect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    notes: List<Note>,
    auth: AuthViewModel?,
    onOpenNote: (Note) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Note) -> Unit,
) {
    val currentUser = auth?.user?.collectAsState()?.value
    val currentUid = currentUser?.uid
    val syncManager = LocalNotesSyncManager.current
    var syncing by remember(currentUid, syncManager) { mutableStateOf(currentUid != null) }

    LaunchedEffect(currentUid, syncManager) {
        syncing = currentUid != null
        if (currentUid != null && syncManager != null) {
            syncManager.events.collect { event ->
                if (event is SyncEvent.SyncCompleted) {
                    syncing = false
                }
            }
        } else {
            syncing = false
        }
    }

    val query = remember { mutableStateOf("") }
    val snackBarHostState = remember { SnackbarHostState() }

    val filteredNotes =
        if (query.value.isBlank()) {
            notes
        } else {
            notes.filter {
                it.title.contains(query.value, ignoreCase = true) ||
                    it.description.contains(query.value, ignoreCase = true)
            }
        }

    val hasNotes = notes.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        ListScreen(
            notes = filteredNotes,
            onNoteClick = onOpenNote,
            onAddClick = onAdd,
            searchQuery = query.value,
            onSearchQueryChange = { query.value = it },
            onDelete = onDelete,
            snackBarHostState = snackBarHostState,
            showTopAppBar = false,
            hasAnyNotes = hasNotes,
            headerContent = null,
            emptyContent = {
                NotesEmptyState(
                    onCreateNote = onAdd,
                )
            },
        )

        if (syncing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}
