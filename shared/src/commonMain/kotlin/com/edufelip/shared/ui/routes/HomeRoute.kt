package com.edufelip.shared.ui.routes

import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import com.edufelip.shared.model.Note
import com.edufelip.shared.auth.AuthController
import com.edufelip.shared.ui.nav.AppRoutes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SnackbarHostState
import com.edufelip.shared.i18n.Str
import com.edufelip.shared.i18n.string
import com.edufelip.shared.auth.AuthUser
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
    onDelete: (Note) -> Unit,
    auth: AuthController?,
    onOpenLogin: () -> Unit,
    onNavigate: (AppRoutes) -> Unit,
) {
    val query = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val closeDrawer = { scope.launch { drawerState.close() } }
    val showLogout = remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    if (auth != null) {
        val currentUser by auth.user.collectAsState()
        val previousUser = remember { mutableStateOf<AuthUser?>(null) }
        LaunchedEffect(currentUser) {
            if (previousUser.value == null && currentUser != null) {
                val nameOrEmail = currentUser.displayName?.takeIf { it.isNotBlank() }
                    ?: currentUser.email?.takeIf { it.isNotBlank() }
                val msg = if (nameOrEmail != null) string(Str.WelcomeUser, nameOrEmail) else string(Str.LoginSuccess)
                snackbarHostState.showSnackbar(msg)
            }
            previousUser.value = currentUser
        }
    }

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
                    onPrivacyClick = null,
                    userName = auth?.user?.value?.displayName,
                    userEmail = auth?.user?.value?.email,
                    onLoginClick = {
                        onOpenLogin()
                        closeDrawer()
                    },
                    onGoogleSignInClick = {
                        // Handled in Login screen; here we can navigate as well
                        onOpenLogin()
                        closeDrawer()
                    },
                    onLogoutClick = { showLogout.value = true }
                )
            },
            onDelete = onDelete,
            darkTheme = darkTheme,
            onToggleDarkTheme = onToggleDarkTheme,
            onOpenTrash = onOpenTrash,
            snackbarHostState = snackbarHostState
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
                    scope.launch { snackbarHostState.showSnackbar(string(Str.SignOutSuccess)) }
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLogout.value = false
                    scope.launch { snackbarHostState.showSnackbar(string(Str.LogoutCanceled)) }
                }) { Text("Cancel") }
            }
        )
    }
}
