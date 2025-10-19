package com.edufelip.shared.ui.nav.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edufelip.shared.model.Folder
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_more
import com.edufelip.shared.resources.folder_layout_grid_content_description
import com.edufelip.shared.resources.folder_layout_list_content_description
import com.edufelip.shared.resources.folder_note_count_one
import com.edufelip.shared.resources.folder_note_count_other
import com.edufelip.shared.resources.folder_options_delete
import com.edufelip.shared.resources.folder_options_rename
import com.edufelip.shared.ui.gadgets.MaterialSearchBar
import org.jetbrains.compose.resources.stringResource

enum class FolderLayout {
    Grid,
    List,
}

@Composable
fun FoldersHeader(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    layoutMode: FolderLayout,
    onToggleLayout: () -> Unit,
    showControls: Boolean = true,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (showControls) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 16.dp, start = 8.dp, end = 8.dp),
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
                    val icon = when (layoutMode) {
                        FolderLayout.Grid -> Icons.Outlined.ViewAgenda
                        FolderLayout.List -> Icons.Outlined.GridView
                    }
                    val description = when (layoutMode) {
                        FolderLayout.Grid -> stringResource(Res.string.folder_layout_list_content_description)
                        FolderLayout.List -> stringResource(Res.string.folder_layout_grid_content_description)
                    }
                    Icon(imageVector = icon, contentDescription = description)
                }
            }
        }
    }
}

@Composable
fun FoldersGrid(
    folders: List<Folder>,
    notesByFolder: Map<Long?, Int>,
    accentPalette: List<Color>,
    onOpenFolder: (Folder) -> Unit,
    onRequestRename: (Folder) -> Unit,
    onRequestDelete: (Folder) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(folders, key = { _, folder -> folder.id }) { index, folder ->
            val accent = accentPalette[index % accentPalette.size]
            FolderGridCard(
                title = folder.name,
                noteCountLabel = folderCountLabel(notesByFolder[folder.id] ?: 0),
                supporting = null,
                accent = accent,
                icon = Icons.Outlined.Folder,
                variant = index + 1,
                onOpen = { onOpenFolder(folder) },
                onRename = { onRequestRename(folder) },
                onDelete = { onRequestDelete(folder) },
            )
        }
    }
}

@Composable
fun FoldersList(
    folders: List<Folder>,
    notesByFolder: Map<Long?, Int>,
    accentPalette: List<Color>,
    onOpenFolder: (Folder) -> Unit,
    onRequestRename: (Folder) -> Unit,
    onRequestDelete: (Folder) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(folders, key = { _, folder -> folder.id }) { index, folder ->
            val accent = accentPalette[index % accentPalette.size]
            FolderListCard(
                title = folder.name,
                noteCountLabel = folderCountLabel(notesByFolder[folder.id] ?: 0),
                supporting = null,
                accent = accent,
                icon = Icons.Outlined.Folder,
                onOpen = { onOpenFolder(folder) },
                onRename = { onRequestRename(folder) },
                onDelete = { onRequestDelete(folder) },
            )
        }
    }
}

@Composable
private fun FolderGridCard(
    title: String,
    noteCountLabel: String,
    supporting: String?,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    variant: Int,
    onOpen: () -> Unit,
    onRename: (() -> Unit)?,
    onDelete: (() -> Unit)?,
) {
    val borderColor = accent.copy(alpha = 0.25f)
    val chipColor = accent.copy(alpha = 0.18f)
    val menuAvailable = onRename != null || onDelete != null

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        onClick = onOpen,
    ) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.20f),
                            accent.copy(alpha = 0.08f),
                        ),
                    ),
                    shape = RoundedCornerShape(24.dp),
                )
                .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Surface(
                    modifier = Modifier.align(Alignment.CenterStart),
                    shape = RoundedCornerShape(20.dp),
                    color = chipColor,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.padding(12.dp),
                    )
                }
                FolderActionsMenu(
                    menuAvailable = menuAvailable,
                    onRename = onRename,
                    onDelete = onDelete,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = noteCountLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            supporting?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            FolderPattern(
                accent = accent,
                variant = variant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
            )
        }
    }
}

@Composable
private fun FolderListCard(
    title: String,
    noteCountLabel: String,
    supporting: String?,
    accent: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onOpen: () -> Unit,
    onRename: (() -> Unit)?,
    onDelete: (() -> Unit)?,
) {
    val chipColor = accent.copy(alpha = 0.18f)
    val menuAvailable = onRename != null || onDelete != null

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp,
        onClick = onOpen,
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.18f),
                            accent.copy(alpha = 0.05f),
                        ),
                    ),
                    shape = RoundedCornerShape(20.dp),
                )
                .border(
                    BorderStroke(1.dp, accent.copy(alpha = 0.15f)),
                    RoundedCornerShape(20.dp),
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = chipColor,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.padding(10.dp),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                    Text(
                        text = noteCountLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    supporting?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            maxLines = 1,
                        )
                    }
                }
                FolderActionsMenu(
                    menuAvailable = menuAvailable,
                    onRename = onRename,
                    onDelete = onDelete,
                )
            }
        }
    }
}

@Composable
private fun FolderActionsMenu(
    menuAvailable: Boolean,
    onRename: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val menuShape = RoundedCornerShape(16.dp)
    val menuContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
    val menuBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)

    Box(modifier = modifier) {
        IconButton(
            onClick = { if (menuAvailable) menuExpanded = true },
            enabled = menuAvailable,
            modifier = Modifier.size(36.dp),
        ) {
            val tint = if (menuAvailable) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            }
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = stringResource(Res.string.cd_more),
                tint = tint,
            )
        }
        if (menuAvailable) {
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier
                    .shadow(12.dp, menuShape, clip = false)
                    .clip(menuShape)
                    .background(menuContainerColor)
                    .border(BorderStroke(1.dp, menuBorderColor), menuShape),
            ) {
                onRename?.let {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(Res.string.folder_options_rename),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            it()
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
                if (onRename != null && onDelete != null) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    )
                }
                onDelete?.let {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(Res.string.folder_options_delete),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            it()
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FolderPattern(
    accent: Color,
    variant: Int,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val colorPrimary = accent.copy(alpha = 0.35f)
        val colorSecondary = accent.copy(alpha = 0.2f)
        val stroke = Stroke(width = size.height * 0.12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        when (variant % 3) {
            0 -> {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.85f)
                    cubicTo(
                        size.width * 0.25f,
                        size.height * 0.35f,
                        size.width * 0.55f,
                        size.height * 1.1f,
                        size.width,
                        size.height * 0.6f,
                    )
                }
                drawPath(path = path, color = colorPrimary, style = stroke)
            }

            1 -> {
                val center = Offset(size.width / 2f, size.height * 0.95f)
                drawCircle(color = colorPrimary, radius = size.width * 0.45f, center = center, style = stroke)
                drawCircle(
                    color = colorSecondary,
                    radius = size.width * 0.28f,
                    center = center.copy(y = center.y - size.height * 0.12f),
                    style = stroke,
                )
            }

            else -> {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.85f)
                    lineTo(size.width * 0.2f, size.height * 0.4f)
                    lineTo(size.width * 0.45f, size.height * 0.65f)
                    lineTo(size.width * 0.7f, size.height * 0.2f)
                    lineTo(size.width, size.height * 0.55f)
                }
                drawPath(path = path, color = colorPrimary, style = stroke)
                drawLine(
                    color = colorSecondary,
                    start = Offset(size.width * 0.4f, size.height * 0.95f),
                    end = Offset(size.width * 0.95f, size.height * 0.35f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
private fun folderCountLabel(count: Int): String = if (count == 1) {
    stringResource(Res.string.folder_note_count_one)
} else {
    stringResource(Res.string.folder_note_count_other, count)
}
