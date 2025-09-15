package com.edufelip.shared.ui.nav.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edufelip.shared.model.Note
import com.edufelip.shared.presentation.AuthViewModel
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.your_notes
import com.edufelip.shared.sync.LocalNotesSyncManager
import com.edufelip.shared.sync.SyncEvent
import com.edufelip.shared.ui.gadgets.DrawerContent
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    notes: List<Note>,
    drawerState: DrawerState,
    darkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    auth: AuthViewModel?,
    onOpenLogin: () -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onOpenNote: (Note) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Note) -> Unit,
    onLogout: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val currentUserForDrawer = auth?.user?.collectAsState()?.value
    val currentUid = currentUserForDrawer?.uid
    val syncManager = LocalNotesSyncManager.current
    var syncing by remember(currentUid, syncManager) { mutableStateOf(currentUid != null) }

    LaunchedEffect(currentUid, syncManager) {
        // When user logs in, show a brief sync indicator until first SyncCompleted
        syncing = currentUid != null
        if (currentUid != null && syncManager != null) {
            syncManager.events.collect { ev ->
                if (ev is SyncEvent.SyncCompleted) {
                    syncing = false
                }
            }
        } else {
            syncing = false
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onYourNotesClick = onOpenDrawer,
                onTrashClick = onNavigateToTrash,
                darkTheme = darkTheme,
                onToggleDarkTheme = onToggleDarkTheme,
                selectedHome = true,
                selectedTrash = false,
                onPrivacyClick = onNavigateToPrivacy,
                userName = currentUserForDrawer?.displayName,
                userEmail = currentUserForDrawer?.email,
                userPhotoUrl = currentUserForDrawer?.photoUrl,
                onLoginClick = onOpenLogin,
                onLogoutClick = {
                    scope.launch { drawerState.close() }
                    onLogout()
                },
            )
        },
    ) {
        Scaffold(
            topBar = {
                Surface(tonalElevation = 2.dp, shadowElevation = 1.dp) {
                    TopAppBar(
                        title = { Text(text = stringResource(Res.string.your_notes)) },
                        navigationIcon = {
                            IconButton(onClick = onOpenDrawer) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = null)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer,
                            navigationIconContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer,
                            titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    )
                }
            },
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                if (syncing) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                HomeContent(
                    notes = notes,
                    drawerState = drawerState,
                    darkTheme = darkTheme,
                    onToggleDarkTheme = onToggleDarkTheme,
                    onOpenTrash = onNavigateToTrash,
                    onOpenNote = onOpenNote,
                    onAdd = onAdd,
                    onDelete = onDelete,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    notes: List<Note>,
    drawerState: DrawerState,
    darkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    onOpenTrash: () -> Unit,
    onOpenNote: (Note) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Note) -> Unit,
) {
    val query = remember { mutableStateOf("") }
    val snackBarHostState = remember { SnackbarHostState() }

    ListScreen(
        notes = if (query.value.isBlank()) {
            notes
        } else {
            notes.filter {
                it.title.contains(
                    query.value,
                    ignoreCase = true
                ) || it.description.contains(query.value, ignoreCase = true)
            }
        },
        onNoteClick = onOpenNote,
        onAddClick = onAdd,
        searchQuery = query.value,
        onSearchQueryChange = { query.value = it },
        drawerState = drawerState,
        drawerContent = null,
        onDelete = onDelete,
        darkTheme = darkTheme,
        onToggleDarkTheme = onToggleDarkTheme,
        onOpenTrash = onOpenTrash,
        snackBarHostState = snackBarHostState,
        managedByShell = true,
        showTopAppBar = false,
        hasAnyNotes = notes.isNotEmpty(),
    )

    // Logout confirmation and loading are handled centrally in AppDrawerScaffold
}
