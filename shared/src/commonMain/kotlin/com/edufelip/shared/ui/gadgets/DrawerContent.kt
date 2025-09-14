package com.edufelip.shared.ui.gadgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.edufelip.shared.resources.*
import com.edufelip.shared.ui.theme.LocalDynamicColorActive
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContent(
    onYourNotesClick: () -> Unit,
    onTrashClick: () -> Unit,
    darkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    selectedHome: Boolean,
    selectedTrash: Boolean,
    onPrivacyClick: (() -> Unit)? = null,
    userName: String? = null,
    userEmail: String? = null,
    userPhotoUrl: String? = null,
    onLoginClick: (() -> Unit)? = null,
    onLogoutClick: (() -> Unit)? = null,
) {
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showLoading by remember { mutableStateOf(false) }

    LaunchedEffect(userEmail) {
        if (showLoading && userEmail == null) {
            showLoading = false
        }
    }

    ModalDrawerSheet {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarImage(photoUrl = userPhotoUrl)
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Column {
                    val text = if (!userEmail.isNullOrEmpty()) {
                        userEmail
                    } else {
                        userName
                    }
                    if (!text.isNullOrEmpty()) {
                        Text(text = text, style = MaterialTheme.typography.titleMedium)
                    } else {
                        Text(
                            text = stringResource(Res.string.guest),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
            HorizontalDivider(
                Modifier,
                DividerDefaults.Thickness,
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
            )
            NavigationDrawerItem(
                label = { Text(stringResource(Res.string.your_notes)) },
                selected = selectedHome,
                onClick = onYourNotesClick,
                icon = {
                    Icon(
                        Icons.Outlined.Book,
                        contentDescription = stringResource(Res.string.your_notes)
                    )
                },
            )
            NavigationDrawerItem(
                label = { Text(stringResource(Res.string.trash)) },
                selected = selectedTrash,
                onClick = onTrashClick,
                icon = {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = stringResource(Res.string.trash)
                    )
                },
            )
            NavigationDrawerItem(
                label = { Text(stringResource(Res.string.privacy_policy)) },
                selected = false,
                onClick = { onPrivacyClick?.invoke() },
                icon = {
                    Icon(
                        Icons.Outlined.Policy,
                        contentDescription = stringResource(Res.string.privacy_policy)
                    )
                },
            )

            Spacer(modifier = Modifier.weight(1f))

            if (onLoginClick != null || onLogoutClick != null) {
                Spacer(modifier = Modifier.padding(vertical = 4.dp))
                if (userEmail == null) {
                    NavigationDrawerItem(
                        label = { Text(stringResource(Res.string.login)) },
                        selected = false,
                        onClick = { onLoginClick?.invoke() },
                        icon = {
                            Icon(
                                Icons.AutoMirrored.Outlined.Login,
                                contentDescription = stringResource(Res.string.login)
                            )
                        },
                    )
                } else {
                    NavigationDrawerItem(
                        label = { Text(stringResource(Res.string.logout)) },
                        selected = false,
                        onClick = { showLogoutConfirm = true },
                        icon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.logout)
                            )
                        },
                    )
                }
            }

            val dynamicActive = LocalDynamicColorActive.current
            if (!dynamicActive) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (darkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = stringResource(Res.string.cd_toggle_dark_theme),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = darkTheme,
                        onCheckedChange = onToggleDarkTheme,
                    )
                }
            }
        }
    }

    // Confirmation dialog for logout
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text(text = stringResource(Res.string.logout_confirm_title)) },
            text = { Text(text = stringResource(Res.string.logout_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        showLoading = true
                        onLogoutClick?.invoke()
                    }
                ) { Text(text = stringResource(Res.string.yes)) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text(text = stringResource(Res.string.no))
                }
            }
        )
    }

    // Loading dialog shown while logging out
    if (showLoading) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 6.dp) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.padding(horizontal = 12.dp))
                    Text(text = stringResource(Res.string.signing_out))
                }
            }
        }
    }
}
