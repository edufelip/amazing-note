package com.edufelip.amazing_note.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.edufelip.amazing_note.ui.nav.AppRoutes
import com.edufelip.amazing_note.ui.screens.AddNoteScreen
import com.edufelip.amazing_note.ui.screens.ListScreen
import com.edufelip.amazing_note.ui.screens.TrashScreen
import com.edufelip.amazing_note.ui.theme.AmazingNoteTheme
import com.edufelip.amazing_note.data.models.Note
import com.edufelip.amazing_note.data.models.Priority
import com.edufelip.amazing_note.ui.viewmodels.NoteViewModel
import com.edufelip.amazing_note.ui.viewmodels.TrashViewModel
import androidx.compose.runtime.livedata.observeAsState
import com.edufelip.amazing_note.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val noteViewModel by viewModels<NoteViewModel>()
    private val trashViewModel by viewModels<TrashViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AmazingNoteTheme {
                AppNavigation(noteViewModel, trashViewModel)
            }
        }
    }
}

@Composable
fun AppNavigation(noteViewModel: NoteViewModel, trashViewModel: TrashViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val backStack = remember { mutableStateListOf<Any>(AppRoutes.Splash) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = { key ->
            entryProviderFactory(
                key = key,
                backStack = backStack,
                noteViewModel = noteViewModel,
                trashViewModel = trashViewModel
            )
        },
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { -it })
        },
        popTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { it })
        }
    )
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
fun entryProviderFactory(
    key: Any,
    backStack: SnapshotStateList<Any>,
    noteViewModel: NoteViewModel,
    trashViewModel: TrashViewModel
): NavEntry<Any> {
    return when (key) {
        is AppRoutes.Splash -> NavEntry(key) {
            SplashScreen(onFinished = {
                backStack.add(AppRoutes.Home)
                backStack.remove(key)
            })
        }
        is AppRoutes.Home -> NavEntry(key) {
            val notes by noteViewModel.noteList.observeAsState(emptyList())
            var query by remember { mutableStateOf("") }
            val searchResults by remember(query) {
                if (query.isBlank()) noteViewModel.noteList else noteViewModel.searchNote(query)
            }.observeAsState(initial = emptyList())

            val listToShow = if (query.isBlank()) notes else searchResults

            ListScreen(
                notes = listToShow,
                onNoteClick = { note -> backStack.add(AppRoutes.NoteDetail(note.id.toString())) },
                onAddClick = { backStack.add(AppRoutes.NoteDetail(null)) },
                searchQuery = query,
                onSearchQueryChange = { query = it },
                drawerContent = {
                    DrawerContent(
                        onTrashClick = { backStack.add(AppRoutes.Trash) }
                    )
                },
                onHamburgerClick = { }
            )
        }
        is AppRoutes.NoteDetail -> NavEntry(key) {
            val notes by noteViewModel.noteList.observeAsState(emptyList())
            val editing: Note? = key.id?.toIntOrNull()?.let { id -> notes.firstOrNull { it.id == id } }
            var title by remember { mutableStateOf(editing?.title ?: "") }
            var description by remember { mutableStateOf(editing?.description ?: "") }
            var priority by remember { mutableStateOf(editing?.priority ?: Priority.LOW) }

            AddNoteScreen(
                title = title,
                onTitleChange = { title = it },
                priority = priority,
                onPriorityChange = { priority = it },
                description = description,
                onDescriptionChange = { description = it },
                onBack = { backStack.removeLastOrNull() },
                onSave = {
                    if (editing == null) {
                        noteViewModel.insertNote(title, priority, description)
                    } else {
                        noteViewModel.updateNote(editing.id, title, priority, description, editing.deleted)
                    }
                    backStack.removeLastOrNull()
                }
            )
        }
        is AppRoutes.Trash -> NavEntry(key) {
            val trash by trashViewModel.deletedNoteList.observeAsState(emptyList())
            TrashScreen(
                notes = trash,
                onBack = { backStack.removeLastOrNull() },
                onNoteClick = { /* Open detail if needed */ }
            )
        }

        else -> {
            error("Unknown route: $key")
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun DrawerContent(onTrashClick: () -> Unit) {
    androidx.compose.material3.NavigationDrawerItem(
        label = { Text("Trash") },
        selected = false,
        onClick = onTrashClick,
        icon = { androidx.compose.material3.Icon(painterResource(id = R.drawable.ic_trash), contentDescription = null) }
    )
}

@Composable
private fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000)
        onFinished()
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Amazing Note",)
    }
}
