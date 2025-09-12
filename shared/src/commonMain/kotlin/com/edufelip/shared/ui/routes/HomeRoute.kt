package com.edufelip.shared.ui.routes

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.edufelip.shared.auth.AuthController
import com.edufelip.shared.auth.AuthUser
import com.edufelip.shared.model.Note
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.login_success
import com.edufelip.shared.resources.logout_canceled
import com.edufelip.shared.resources.sign_out_success
import com.edufelip.shared.resources.welcome_user
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.screens.ListScreen
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

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
    onDelete: (Note) -> Unit,
    auth: AuthController?,
    onOpenLogin: () -> Unit,
    onNavigate: (AppRoutes) -> Unit,
    onOpenPrivacy: () -> Unit = {},
) {
    val query = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val closeDrawer = { scope.launch { drawerState.close() } }
    val showLogout = remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val signOutSuccessText = stringResource(Res.string.sign_out_success)
    val logoutCanceledText = stringResource(Res.string.logout_canceled)

    if (auth != null) {
        val currentUser by auth.user.collectAsState()
        val previousUser = remember { mutableStateOf<AuthUser?>(null) }
        val nameOrEmail = currentUser?.displayName?.takeIf { it.isNotBlank() }
            ?: currentUser?.email?.takeIf { it.isNotBlank() }
        val loginSuccessText = if (nameOrEmail != null) stringResource(Res.string.welcome_user, nameOrEmail) else stringResource(Res.string.login_success)
        LaunchedEffect(currentUser) {
            val cu = currentUser
            if (previousUser.value == null && cu != null) {
                snackbarHostState.showSnackbar(loginSuccessText)
            }
            previousUser.value = cu
        }
    }

    ListScreen(
        notes = if (query.value.isBlank()) {
            notes
        } else {
            notes.filter {
                it.title.contains(query.value, ignoreCase = true) || it.description.contains(query.value, ignoreCase = true)
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
        snackbarHostState = snackbarHostState,
        managedByShell = true,
        showTopAppBar = false,
    )

    if (showLogout.value) {
        AlertDialog(
            onDismissRequest = { showLogout.value = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogout.value = false
                    auth?.logout()
                    scope.launch { snackbarHostState.showSnackbar(signOutSuccessText) }
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLogout.value = false
                    scope.launch { snackbarHostState.showSnackbar(logoutCanceledText) }
                }) { Text("Cancel") }
            },
        )
    }
}
