package com.edufelip.shared.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.edufelip.shared.model.Note
import com.edufelip.shared.ui.gadgets.DismissibleNoteRow
import com.edufelip.shared.ui.gadgets.SearchView
import kotlinx.coroutines.launch
import com.edufelip.shared.i18n.Str
import com.edufelip.shared.i18n.string

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
    onDelete: (Note) -> Unit
) {
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { if (drawerContent != null) drawerContent() }
    ) {
        Scaffold(
            topBar = {
                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp)
                ) {
                    // Large header area like legacy CollapsingToolbar title
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(0.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .padding(0.dp)
                        ) {
                            // Background color
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .padding(0.dp)
                            )
                        }
                        Text(
                            text = string(Str.YourNotes),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Toolbar row: hamburger + search view
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = string(Str.CdOpenDrawer),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Box(modifier = Modifier
                            .padding(start = 8.dp, top = 2.dp, bottom = 2.dp)
                            .weight(1f)) {
                            SearchView(
                                query = searchQuery,
                                onQueryChange = onSearchQueryChange
                            )
                        }
                    }
                }
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = string(Str.CdAdd),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(top = 0.dp),
            ) {
                LazyColumn(
                    contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        DismissibleNoteRow(
                            note = note,
                            onClick = onNoteClick,
                            onDismiss = onDelete,
                            isRestore = false,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
