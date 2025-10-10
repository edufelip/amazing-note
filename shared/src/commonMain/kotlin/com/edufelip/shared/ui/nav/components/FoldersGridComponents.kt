package com.edufelip.shared.ui.nav.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.edufelip.shared.model.Folder
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_more
import com.edufelip.shared.resources.folder_note_count_one
import com.edufelip.shared.resources.folder_note_count_other
import com.edufelip.shared.resources.folder_options_delete
import com.edufelip.shared.resources.folder_options_rename
import com.edufelip.shared.resources.folders_header_subtitle
import com.edufelip.shared.resources.folders_header_title
import com.edufelip.shared.resources.unassigned_notes
import com.edufelip.shared.resources.unassigned_subtitle
import org.jetbrains.compose.resources.stringResource

@Composable
fun FoldersGrid(
    folders: List<Folder>,
    unassignedCount: Int,
    notesByFolder: Map<Long?, Int>,
    accentPalette: List<Color>,
    onOpenFolder: (Folder) -> Unit,
    onOpenUnassigned: () -> Unit,
    onRequestRename: (Folder) -> Unit,
    onRequestDelete: (Folder) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(2),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxCurrentLineSpan) }) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.folders_header_title),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.folders_header_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }

        item {
            FolderGridCard(
                title = stringResource(Res.string.unassigned_notes),
                noteCountLabel = folderCountLabel(unassignedCount),
                supporting = stringResource(Res.string.unassigned_subtitle),
                accent = MaterialTheme.colorScheme.primary,
                icon = Icons.Outlined.Description,
                variant = 0,
                onOpen = onOpenUnassigned,
                onRename = null,
                onDelete = null,
            )
        }

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

        item(span = { GridItemSpan(maxCurrentLineSpan) }) {
            Spacer(modifier = Modifier.height(56.dp))
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
    var menuExpanded by remember { mutableStateOf(false) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
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
                IconButton(
                    onClick = { if (menuAvailable) menuExpanded = true },
                    enabled = menuAvailable,
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
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        onRename?.let {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(Res.string.folder_options_rename)) },
                                onClick = {
                                    menuExpanded = false
                                    it()
                                },
                            )
                        }
                        onDelete?.let {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Outlined.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(end = 8.dp),
                                        )
                                        Text(text = stringResource(Res.string.folder_options_delete))
                                    }
                                },
                                onClick = {
                                    menuExpanded = false
                                    it()
                                },
                            )
                        }
                    }
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
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
