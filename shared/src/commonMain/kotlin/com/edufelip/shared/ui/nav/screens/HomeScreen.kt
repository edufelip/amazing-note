package com.edufelip.shared.ui.nav.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edufelip.shared.auth.AuthController
import com.edufelip.shared.auth.AuthUser
import com.edufelip.shared.model.Note
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.login_success
import com.edufelip.shared.resources.logout_canceled
import com.edufelip.shared.resources.sign_out_success
import com.edufelip.shared.resources.welcome_user
import com.edufelip.shared.resources.your_notes
import com.edufelip.shared.ui.gadgets.DrawerContent
import com.edufelip.shared.ui.nav.AppRoutes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    notes: List<Note>,
    drawerState: DrawerState,
    darkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    auth: AuthController?,
    onOpenLogin: () -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onOpenNote: (Note) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Note) -> Unit,
    onLogout: () -> Unit,
) {
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
                userName = auth?.user?.value?.displayName,
                userEmail = auth?.user?.value?.email,
                userPhotoUrl = auth?.user?.value?.photoUrl,
                onLoginClick = onOpenLogin,
                onLogoutClick = onLogout,
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
            Box(modifier = Modifier.padding(padding)) {
                HomeContent(
                    notes = notes,
                    drawerState = drawerState,
                    darkTheme = darkTheme,
                    onToggleDarkTheme = onToggleDarkTheme,
                    onOpenTrash = onNavigateToTrash,
                    onOpenNote = onOpenNote,
                    onAdd = onAdd,
                    onDelete = onDelete,
                    auth = auth,
                    onOpenLogin = onOpenLogin,
                    onNavigate = { /* unused */ },
                    onOpenPrivacy = onNavigateToPrivacy,
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
        val loginSuccessText = if (nameOrEmail != null) stringResource(
            Res.string.welcome_user,
            nameOrEmail
        ) else stringResource(Res.string.login_success)
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
        snackbarHostState = snackbarHostState,
        managedByShell = true,
        showTopAppBar = false,
        hasAnyNotes = notes.isNotEmpty(),
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
