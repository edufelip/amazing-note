package com.edufelip.shared.ui.gadgets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.edufelip.shared.i18n.Str
import com.edufelip.shared.i18n.string
import com.edufelip.shared.ui.theme.LocalDynamicColorActive

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
) {
    ModalDrawerSheet {
        NavigationDrawerItem(
            label = { Text(string(Str.YourNotes)) },
            selected = selectedHome,
            onClick = onYourNotesClick,
            icon = { Icon(Icons.Default.Book, contentDescription = string(Str.YourNotes)) }
        )
        NavigationDrawerItem(
            label = { Text(string(Str.Trash)) },
            selected = selectedTrash,
            onClick = onTrashClick,
            icon = { Icon(Icons.Default.Delete, contentDescription = string(Str.Trash)) }
        )
        NavigationDrawerItem(
            label = { Text(string(Str.PrivacyPolicy)) },
            selected = false,
            onClick = { onPrivacyClick?.invoke() },
            icon = { Icon(Icons.Default.Book, contentDescription = string(Str.PrivacyPolicy)) }
        )

        Spacer(modifier = Modifier.weight(1f))

        val dynamicActive = LocalDynamicColorActive.current
        if (!dynamicActive) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (darkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                    contentDescription = string(Str.CdToggleDarkTheme),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = darkTheme,
                    onCheckedChange = onToggleDarkTheme
                )
            }
        }
    }
}
