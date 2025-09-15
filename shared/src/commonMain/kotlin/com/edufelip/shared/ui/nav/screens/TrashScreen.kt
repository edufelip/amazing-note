package com.edufelip.shared.ui.nav.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.edufelip.shared.presentation.AuthViewModel
import com.edufelip.shared.model.Note
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.empty_trash_hint
import com.edufelip.shared.resources.empty_trash_title
import com.edufelip.shared.resources.trash
import com.edufelip.shared.ui.gadgets.DismissibleNoteRow
import com.edufelip.shared.ui.gadgets.DrawerContent
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    notes: List<Note>,
    drawerState: DrawerState,
    darkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    auth: AuthViewModel?,
    onOpenLogin: () -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onRestore: (Note) -> Unit,
    onLogout: () -> Unit
) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val currentUserForDrawer = auth?.user?.collectAsState()?.value

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onYourNotesClick = onNavigateToHome,
                onTrashClick = onOpenDrawer,
                darkTheme = darkTheme,
                onToggleDarkTheme = onToggleDarkTheme,
                selectedHome = false,
                selectedTrash = true,
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
                        title = { Text(text = stringResource(Res.string.trash)) },
                        navigationIcon = {
                            IconButton(onClick = onOpenDrawer) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = null)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ),
                    )
                }
            },
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                TrashScreenContent(
                    notes = notes,
                    onRestore = onRestore,
                )
            }
        }
    }
}

@Composable
fun TrashScreenContent(
    notes: List<Note>,
    onRestore: (Note) -> Unit,
) {
    if (notes.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.empty_trash_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = stringResource(Res.string.empty_trash_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }

    LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)) {
        items(notes, key = { it.id }) { note ->
            DismissibleNoteRow(
                note = note,
                onClick = onRestore,
                onDismiss = onRestore,
                isRestore = true,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}
