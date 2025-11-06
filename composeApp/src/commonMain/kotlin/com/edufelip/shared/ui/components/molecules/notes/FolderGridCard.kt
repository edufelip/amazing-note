package com.edufelip.shared.ui.components.molecules.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.times
import com.edufelip.shared.ui.components.atoms.graphics.FolderPattern
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews

@Composable
fun FolderGridCard(
    title: String,
    noteCountLabel: String,
    accent: Color,
    icon: ImageVector,
    variant: Int,
    onOpen: () -> Unit,
    onRename: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    supporting: String? = null,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues? = null,
) {
    val tokens = designTokens()
    val borderColor = accent.copy(alpha = 0.25f)
    val chipColor = accent.copy(alpha = 0.18f)
    val menuAvailable = onRename != null || onDelete != null
    val cardShape = RoundedCornerShape(tokens.radius.lg * 2)
    val chipShape = RoundedCornerShape(tokens.radius.lg)
    val patternHeight = tokens.spacing.xxl * 2f + tokens.spacing.md

    Surface(
        modifier = modifier,
        shape = cardShape,
        color = Color.Transparent,
        tonalElevation = tokens.elevation.card,
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
                    shape = cardShape,
                )
                .border(BorderStroke(androidx.compose.ui.unit.Dp.Hairline, borderColor), cardShape)
                .padding(contentPadding ?: PaddingValues(tokens.spacing.xl)),
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier.align(Alignment.CenterStart),
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
                    .height(patternHeight),
            )
        }
    }
}

@DevicePreviews
@Composable
private fun FolderGridCardPreview() {
    DevicePreviewContainer {
        FolderGridCard(
            title = "Personal",
            noteCountLabel = "12 notes",
            accent = Color(0xFF4E6CEF),
            icon = Icons.Outlined.Folder,
            variant = 1,
            onOpen = {},
            onRename = {},
            onDelete = {},
        )
    }
}
