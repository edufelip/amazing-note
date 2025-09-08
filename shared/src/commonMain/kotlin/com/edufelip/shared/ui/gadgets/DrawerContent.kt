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
            label = { Text("Your Notes") },
            selected = selectedHome,
            onClick = onYourNotesClick,
            icon = { Icon(Icons.Default.Book, contentDescription = null) }
        )
        NavigationDrawerItem(
            label = { Text("Trash") },
            selected = selectedTrash,
            onClick = onTrashClick,
            icon = { Icon(Icons.Default.Delete, contentDescription = null) }
        )
        NavigationDrawerItem(
            label = { Text("Privacy Policy") },
            selected = false,
            onClick = { onPrivacyClick?.invoke() },
            icon = { Icon(Icons.Default.Book, contentDescription = null) }
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (darkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                contentDescription = null,
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

