package com.edufelip.shared.ui.nav.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.delete_folder_message
import com.edufelip.shared.resources.folder_add_note
import com.edufelip.shared.resources.folder_detail_empty_hint
import com.edufelip.shared.resources.folder_detail_empty_title
import com.edufelip.shared.resources.folder_options_delete
import com.edufelip.shared.resources.folder_options_rename
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(
    title: String,
    notes: List<Note>,
    onBack: () -> Unit,
    onOpenNote: (Note) -> Unit,
    onAddNote: () -> Unit,
    onDeleteNote: (Note) -> Unit,
    onRenameFolder: ((String) -> Unit)? = null,
    onDeleteFolder: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var renameDialog by remember { mutableStateOf(false) }
    var deleteDialog by remember { mutableStateOf(false) }
    var nameInput by rememberSaveable { mutableStateOf(title) }
    var menuExpanded by remember { mutableStateOf(false) }

    val filteredNotes = remember(notes, query) {
        if (query.isBlank()) {
            notes
        } else {
            notes.filter { note ->
                note.title.contains(query, ignoreCase = true) ||
                    note.description.contains(query, ignoreCase = true)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (onRenameFolder != null || onDeleteFolder != null) {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            if (onRenameFolder != null) {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(Res.string.folder_options_rename)) },
                                    onClick = {
                                        menuExpanded = false
                                        nameInput = title
                                        renameDialog = true
                                    },
                                )
                            }
                            if (onDeleteFolder != null) {
                                DropdownMenuItem(
                                    text = { Text(text = stringResource(Res.string.folder_options_delete)) },
                                    onClick = {
                                        menuExpanded = false
                                        deleteDialog = true
                                    },
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddNote) {
                Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(Res.string.folder_add_note))
            }
        },
    ) { padding ->
        if (notes.isEmpty() && query.isBlank()) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.folder_detail_empty_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.folder_detail_empty_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            ListScreen(
                notes = filteredNotes,
                onNoteClick = onOpenNote,
                onAddClick = onAddNote,
                searchQuery = query,
                onSearchQueryChange = { query = it },
                onDelete = onDeleteNote,
                showTopAppBar = false,
                hasAnyNotes = notes.isNotEmpty(),
                title = title,
            )
        }
    }

    if (renameDialog && onRenameFolder != null) {
        FolderNameDialog(
            title = stringResource(Res.string.folder_options_rename),
            initialValue = nameInput,
            onDismiss = { renameDialog = false },
            onConfirm = { value ->
                if (value.isNotBlank()) onRenameFolder(value.trim())
                renameDialog = false
            },
        )
    }

    if (deleteDialog && onDeleteFolder != null) {
        DeleteFolderDialog(
            message = stringResource(Res.string.delete_folder_message),
            onDismiss = { deleteDialog = false },
            onConfirm = {
                onDeleteFolder()
                deleteDialog = false
            },
        )
    }
}
