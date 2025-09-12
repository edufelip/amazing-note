package com.edufelip.shared.ui.gadgets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_toggle_dark_theme
import com.edufelip.shared.resources.google_sign_in
import com.edufelip.shared.resources.guest
import com.edufelip.shared.resources.login
import com.edufelip.shared.resources.logout
import com.edufelip.shared.resources.privacy_policy
import com.edufelip.shared.resources.trash
import com.edufelip.shared.resources.your_notes
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
    // Auth-related
    userName: String? = null,
    userEmail: String? = null,
    userPhotoUrl: String? = null,
    onLoginClick: (() -> Unit)? = null,
    onGoogleSignInClick: (() -> Unit)? = null,
    onLogoutClick: (() -> Unit)? = null,
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarImage(photoUrl = userPhotoUrl, modifier = Modifier)
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Column(modifier = Modifier) {
                    if (userEmail != null) {
                        Text(text = userName ?: userEmail, style = MaterialTheme.typography.titleMedium)
                    } else {
                        Text(text = stringResource(Res.string.guest), style = MaterialTheme.typography.titleMedium)
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
                icon = { Icon(Icons.Default.Book, contentDescription = stringResource(Res.string.your_notes)) },
            )
            NavigationDrawerItem(
                label = { Text(stringResource(Res.string.trash)) },
                selected = selectedTrash,
                onClick = onTrashClick,
                icon = { Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.trash)) },
            )
            NavigationDrawerItem(
                label = { Text(stringResource(Res.string.privacy_policy)) },
                selected = false,
                onClick = { onPrivacyClick?.invoke() },
                icon = { Icon(Icons.Default.Book, contentDescription = stringResource(Res.string.privacy_policy)) },
            )

            Spacer(modifier = Modifier.weight(1f))

            // Auth block
            if (onLoginClick != null || onLogoutClick != null) {
                Spacer(modifier = Modifier.padding(vertical = 4.dp))
                if (userEmail == null) {
                    // Not authenticated: show Login + Google
                    NavigationDrawerItem(
                        label = { Text(stringResource(Res.string.login)) },
                        selected = false,
                        onClick = { onLoginClick?.invoke() },
                        icon = { Icon(Icons.Default.Book, contentDescription = stringResource(Res.string.login)) },
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(Res.string.google_sign_in)) },
                        selected = false,
                        onClick = { onGoogleSignInClick?.invoke() },
                        icon = { Icon(Icons.Default.Book, contentDescription = stringResource(Res.string.google_sign_in)) },
                    )
                } else {
                    NavigationDrawerItem(
                        label = { Text(userName ?: userEmail) },
                        selected = false,
                        onClick = {},
                        icon = { Icon(Icons.Default.Book, contentDescription = userName ?: userEmail) },
                    )
                    NavigationDrawerItem(
                        label = { Text(stringResource(Res.string.logout)) },
                        selected = false,
                        onClick = { onLogoutClick?.invoke() },
                        icon = { Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.logout)) },
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
}
