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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.folder_layout_grid_content_description
import com.edufelip.shared.resources.folder_layout_list_content_description
import com.edufelip.shared.ui.components.molecules.common.MaterialSearchBar
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
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
    val tokens = designTokens()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.lg),
    ) {
        if (showControls) {
            Row(
                modifier = Modifier.padding(
                    top = tokens.spacing.xl,
                    start = tokens.spacing.sm,
                    end = tokens.spacing.sm,
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
            ) {
                MaterialSearchBar(
                    modifier = Modifier.weight(1f),
                    query = query,
                    onQueryChange = onQueryChange,
                )
                FilledTonalIconButton(
                    onClick = onToggleLayout,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = tokens.colors.accentMuted,
                        contentColor = tokens.colors.onSurface,
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

@DevicePreviews
@Composable
private fun FoldersHeaderPreview() {
    val (query, setQuery) = remember { mutableStateOf("Work") }
    DevicePreviewContainer {
        FoldersHeader(
            query = query,
            onQueryChange = setQuery,
            layoutMode = FolderLayout.Grid,
            onToggleLayout = {},
        )
    }
}
