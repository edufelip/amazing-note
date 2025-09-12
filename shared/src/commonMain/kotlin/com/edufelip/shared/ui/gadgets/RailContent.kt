package com.edufelip.shared.ui.gadgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_toggle_dark_theme
import com.edufelip.shared.resources.trash
import com.edufelip.shared.resources.your_notes
import com.edufelip.shared.ui.theme.LocalDynamicColorActive
import org.jetbrains.compose.resources.stringResource

@Composable
fun RailContent(
    onYourNotesClick: () -> Unit,
    onTrashClick: () -> Unit,
    darkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    selectedHome: Boolean,
    selectedTrash: Boolean,
) {
    val dynamicActive = LocalDynamicColorActive.current
    NavigationRail {
        Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxHeight()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                NavigationRailItem(
                    selected = selectedHome,
                    onClick = onYourNotesClick,
                    icon = { Icon(Icons.Default.Book, contentDescription = stringResource(Res.string.your_notes)) },
                    label = { Text(stringResource(Res.string.your_notes)) },
                )
                NavigationRailItem(
                    selected = selectedTrash,
                    onClick = onTrashClick,
                    icon = { Icon(Icons.Default.Delete, contentDescription = stringResource(Res.string.trash)) },
                    label = { Text(stringResource(Res.string.trash)) },
                )
            }
            if (!dynamicActive) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (darkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = stringResource(Res.string.cd_toggle_dark_theme),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = androidx.compose.ui.Modifier.padding(4.dp))
                    Switch(checked = darkTheme, onCheckedChange = onToggleDarkTheme)
                }
            }
        }
    }
}
