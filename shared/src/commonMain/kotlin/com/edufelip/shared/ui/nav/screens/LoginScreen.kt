package com.edufelip.shared.ui.nav.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.edufelip.shared.auth.AuthController
import com.edufelip.shared.auth.AuthUser
import com.edufelip.shared.model.Note
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.email
import com.edufelip.shared.resources.forgot_password
import com.edufelip.shared.resources.google_sign_in
import com.edufelip.shared.resources.google_sign_in_canceled
import com.edufelip.shared.resources.login
import com.edufelip.shared.resources.login_success
import com.edufelip.shared.resources.login_title
import com.edufelip.shared.resources.logout_canceled
import com.edufelip.shared.resources.password
import com.edufelip.shared.resources.reset_email_sent
import com.edufelip.shared.resources.sign_out_success
import com.edufelip.shared.resources.sign_up
import com.edufelip.shared.resources.sign_up_success
import com.edufelip.shared.resources.welcome_user
import com.edufelip.shared.resources.your_notes
import com.edufelip.shared.ui.gadgets.DrawerContent
import com.edufelip.shared.ui.nav.AppRoutes
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    auth: AuthController,
    onBack: () -> Unit,
    onRequestGoogleSignIn: (((Boolean, String?) -> Unit) -> Unit)? = null,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val loading by auth.loading.collectAsState()
    val error by auth.error.collectAsState()
    val user by auth.user.collectAsState()
    val message by auth.message.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val loginSuccessText = stringResource(Res.string.login_success)
    LaunchedEffect(user) {
        if (user != null) {
            snackbarHostState.showSnackbar(loginSuccessText)
            delay(400)
            onBack()
        }
    }
    val resetEmailSentText = stringResource(Res.string.reset_email_sent)
    val signUpSuccessText = stringResource(Res.string.sign_up_success)
    LaunchedEffect(message) {
        when (message) {
            "RESET_EMAIL_SENT" -> {
                snackbarHostState.showSnackbar(resetEmailSentText)
                auth.clearMessage()
            }
            "SIGN_UP_SUCCESS" -> {
                snackbarHostState.showSnackbar(signUpSuccessText)
                auth.clearMessage()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = stringResource(Res.string.login_title))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(Res.string.email)) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(Res.string.password)) },
                modifier = Modifier.fillMaxWidth(),
            )

            LaunchedEffect(error) {
                if (error != null) {
                    snackbarHostState.showSnackbar(error ?: "")
                    auth.clearError()
                }
            }

            Button(
                onClick = { auth.loginWithEmail(email.trim(), password) },
                enabled = email.isNotBlank() && password.isNotBlank() && !loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Text(stringResource(Res.string.login))
                }
            }

            val scope = rememberCoroutineScope()
            val googleCanceledText = stringResource(Res.string.google_sign_in_canceled)
            Button(
                onClick = {
                    onRequestGoogleSignIn?.invoke { success, err ->
                        if (!success) {
                            // Show Google sign-in error or cancellation
                            val msg = err ?: googleCanceledText
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.google_sign_in))
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = {
                if (email.isNotBlank()) auth.sendPasswordReset(email.trim())
            }, enabled = !loading) {
                Text(stringResource(Res.string.forgot_password))
            }
            TextButton(onClick = {
                if (email.isNotBlank() && password.isNotBlank()) auth.signUp(email.trim(), password)
            }, enabled = !loading) {
                Text(stringResource(Res.string.sign_up))
            }
        }
    }
}

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