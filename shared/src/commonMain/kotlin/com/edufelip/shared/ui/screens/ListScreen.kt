package com.edufelip.shared.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.edufelip.shared.i18n.Str
import com.edufelip.shared.i18n.string
import com.edufelip.shared.model.Note
import com.edufelip.shared.ui.gadgets.DismissibleNoteRow
import com.edufelip.shared.ui.gadgets.MaterialSearchBar
import com.edufelip.shared.ui.gadgets.RailContent
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
@Composable
fun ListScreen(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onAddClick: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    drawerState: DrawerState,
    drawerContent: (@Composable () -> Unit)? = null,
    onDelete: (Note) -> Unit,
    // For large screens with NavigationRail
    darkTheme: Boolean = false,
    onToggleDarkTheme: (Boolean) -> Unit = {},
    onOpenTrash: (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState? = null
) {
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLarge = maxWidth > 600.dp
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

        @Composable
        fun ContentScaffold(showNavIcon: Boolean) {
            Scaffold(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                snackbarHost = {
                    if (snackbarHostState != null) SnackbarHost(snackbarHostState)
                },
                topBar = {
                    LargeTopAppBar(
                        title = { Text(text = string(Str.YourNotes)) },
                        navigationIcon = {
                            if (showNavIcon) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = string(Str.CdOpenDrawer)
                                    )
                                }
                            }
                        },
                        scrollBehavior = scrollBehavior
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = onAddClick,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CircleShape
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = string(Str.CdAdd),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        stickyHeader {
                            Surface(
                                tonalElevation = 3.dp,
                                shadowElevation = 1.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    MaterialSearchBar(
                                        query = searchQuery,
                                        onQueryChange = onSearchQueryChange
                                    )
                                }
                            }
                        }
                        items(notes, key = { it.id }) { note ->
                            DismissibleNoteRow(
                                note = note,
                                onClick = onNoteClick,
                                onDismiss = onDelete,
                                isRestore = false,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .animateItem()
                            )
                        }
                    }
                }
            }
        }

        if (isLarge) {
            Row(modifier = Modifier.fillMaxSize()) {
                RailContent(
                    onYourNotesClick = { /* already on home */ },
                    onTrashClick = { onOpenTrash?.invoke() },
                    darkTheme = darkTheme,
                    onToggleDarkTheme = onToggleDarkTheme,
                    selectedHome = true,
                    selectedTrash = false
                )
                Box(modifier = Modifier.weight(1f)) {
                    ContentScaffold(showNavIcon = false)
                }
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = { if (drawerContent != null) drawerContent() }
            ) {
                ContentScaffold(showNavIcon = true)
            }
        }
    }
}
