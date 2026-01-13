package com.edufelip.shared.ui.components.organisms.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
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
    showSearchBar: Boolean = true,
) {
    val tokens = designTokens()
    Column(
        modifier = modifier,
    ) {
        if (showControls) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
            ) {
                androidx.compose.foundation.layout.Box(Modifier.weight(1f)) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showSearchBar,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                    ) {
                        MaterialSearchBar(
                            modifier = Modifier,
                            query = query,
                            onQueryChange = onQueryChange,
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = showSearchBar,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
                    ) {
                        Spacer(modifier = Modifier.width(tokens.spacing.sm))
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
