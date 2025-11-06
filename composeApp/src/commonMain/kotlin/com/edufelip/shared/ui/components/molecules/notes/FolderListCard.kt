package com.edufelip.shared.ui.components.molecules.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews

@Composable
fun FolderListCard(
    title: String,
    noteCountLabel: String,
    accent: Color,
    icon: ImageVector,
    onOpen: () -> Unit,
    onRename: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    supporting: String? = null,
    modifier: Modifier = Modifier,
) {
    val tokens = designTokens()
    val chipColor = accent.copy(alpha = 0.18f)
    val menuAvailable = onRename != null || onDelete != null
    val cardShape = RoundedCornerShape(tokens.radius.lg + tokens.radius.sm)
    val chipShape = RoundedCornerShape(tokens.radius.md + tokens.radius.sm)

    Surface(
        modifier = modifier,
        shape = cardShape,
        color = Color.Transparent,
        tonalElevation = tokens.elevation.card,
        onClick = onOpen,
    ) {
        Row(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.18f),
                            accent.copy(alpha = 0.05f),
                        ),
                    ),
                    shape = cardShape,
                )
                .border(
                    BorderStroke(androidx.compose.ui.unit.Dp.Hairline, accent.copy(alpha = 0.15f)),
                    cardShape,
                )
                .padding(
                    horizontal = tokens.spacing.xl,
                    vertical = tokens.spacing.md,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md + tokens.spacing.sm),
        ) {
            Surface(
                shape = chipShape,
                color = chipColor,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.padding(tokens.spacing.md),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(tokens.spacing.xs),
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

@DevicePreviews
@Composable
private fun FolderListCardPreview() {
    DevicePreviewContainer {
        FolderListCard(
            title = "Archive",
            noteCountLabel = "3 notes",
            accent = Color(0xFF00897B),
            icon = Icons.Outlined.Folder,
            onOpen = {},
            onRename = {},
            onDelete = {},
        )
    }
}
