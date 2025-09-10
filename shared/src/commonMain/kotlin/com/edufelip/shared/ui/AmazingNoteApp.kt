package com.edufelip.shared.ui

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
import com.edufelip.shared.auth.AuthController
import com.edufelip.shared.auth.AuthService
import com.edufelip.shared.auth.NoAuthService
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.routes.HomeRoute
import com.edufelip.shared.ui.routes.NoteDetailRoute
import com.edufelip.shared.ui.routes.TrashRoute
import com.edufelip.shared.ui.screens.LoginScreen
import com.edufelip.shared.ui.theme.AmazingNoteTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmazingNoteApp(
    viewModel: NoteUiViewModel,
    authService: AuthService = NoAuthService,
    onRequestGoogleSignIn: (((Boolean, String?) -> Unit) -> Unit)? = null,
) {
    var darkTheme by rememberSaveable { mutableStateOf(false) }
    val backStack = remember { mutableStateListOf<AppRoutes>(AppRoutes.Home) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val notes by viewModel.notes.collectAsState(initial = emptyList())
    val trash by viewModel.trash.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val auth = remember(authService) { AuthController(authService, scope) }

    AmazingNoteTheme(darkTheme = darkTheme) {
        when (val current = backStack.last()) {
            is AppRoutes.Home -> {
                HomeRoute(
                    notes = notes,
                    drawerState = drawerState,
                    darkTheme = darkTheme,
                    onToggleDarkTheme = { darkTheme = it },
                    onOpenTrash = { backStack.add(AppRoutes.Trash) },
                    onOpenNote = { note -> backStack.add(AppRoutes.NoteDetail(note.id)) },
                    onAdd = { backStack.add(AppRoutes.NoteDetail(null)) },
                    onDelete = { note -> scope.launch { viewModel.setDeleted(note.id, true) } },
                    auth = auth,
                    onOpenLogin = { backStack.add(AppRoutes.Login) },
                    onNavigate = { route -> backStack.add(route) }
                )
            }
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
                )
            }
            is AppRoutes.Trash -> {
                TrashRoute(
                    notes = trash,
                    onBack = { backStack.removeLastOrNull() },
                    onRestore = { note ->
                        scope.launch { viewModel.setDeleted(note.id, false) }
                    }
                )
            }
            is AppRoutes.Login -> {
                LoginScreen(
                    auth = auth,
                    onBack = { backStack.removeLastOrNull() },
                    onRequestGoogleSignIn = onRequestGoogleSignIn
                )
            }
        }
    }
}
