package com.edufelip.shared.ui.components.organisms.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.edufelip.shared.preview.Preview
import androidx.compose.ui.unit.dp
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.folder_layout_grid_content_description
import com.edufelip.shared.resources.folder_layout_list_content_description
import com.edufelip.shared.ui.components.molecules.common.MaterialSearchBar
import org.jetbrains.compose.resources.stringResource

enum class FolderLayout {
    Grid,
    List,
}

@Composable
fun FoldersHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    layoutMode: FolderLayout,
    onToggleLayout: () -> Unit,
    modifier: Modifier = Modifier,
    showControls: Boolean = true,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (showControls) {
            Row(
                modifier = Modifier.padding(top = 16.dp, start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                MaterialSearchBar(
                    modifier = Modifier.weight(1f),
                    query = query,
                    onQueryChange = onQueryChange,
                )
                FilledTonalIconButton(
                    onClick = onToggleLayout,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                ) {
                    val (icon, description) = when (layoutMode) {
                        FolderLayout.Grid -> Icons.Outlined.ViewAgenda to stringResource(Res.string.folder_layout_list_content_description)
                        FolderLayout.List -> Icons.Outlined.GridView to stringResource(Res.string.folder_layout_grid_content_description)
                    }
                    Icon(imageVector = icon, contentDescription = description)
                }
            }
        }
    }
}

@Preview
@Composable
private fun FoldersHeaderPreview() {
    val (query, setQuery) = remember { mutableStateOf("Work") }
    FoldersHeader(
        query = query,
        onQueryChange = setQuery,
        layoutMode = FolderLayout.Grid,
        onToggleLayout = {},
    )
}
