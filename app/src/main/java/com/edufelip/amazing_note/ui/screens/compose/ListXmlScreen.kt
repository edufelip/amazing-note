package com.edufelip.amazing_note.ui.screens.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.edufelip.amazing_note.R
import com.edufelip.amazing_note.data.models.Note

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun ListXmlScreen(
    notes: List<Note>,
    onNoteClick: (Note) -> Unit,
    onAddClick: () -> Unit,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    drawerContent: (@Composable () -> Unit)? = null,
    onHamburgerClick: () -> Unit = {}
) {
    val drawerOpen = remember { mutableStateOf(false) }
    ModalNavigationDrawer(
        drawerContent = {
            if (drawerContent != null) drawerContent()
        },
        gesturesEnabled = drawerContent != null && drawerOpen.value,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Your notes") },
                    navigationIcon = {
                        IconButton(onClick = {
                            drawerOpen.value = !drawerOpen.value
                            onHamburgerClick()
                        }) {
                            Icon(painter = painterResource(id = R.drawable.ic_menu), contentDescription = null)
                        }
                    },
                    actions = {
                        SearchViewCompose(query = searchQuery, onQueryChange = onSearchQueryChange, modifier = Modifier.padding(end = 8.dp))
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onAddClick) {
                    Icon(painter = painterResource(id = R.drawable.ic_add), contentDescription = null)
                }
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(
                    contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteRow(note = note, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), onClick = onNoteClick)
                    }
                }
            }
        }
    }
}
