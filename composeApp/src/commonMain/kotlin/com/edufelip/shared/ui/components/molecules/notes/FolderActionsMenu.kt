package com.edufelip.shared.ui.components.molecules.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.times
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_more
import com.edufelip.shared.resources.folder_options_delete
import com.edufelip.shared.resources.folder_options_rename
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import org.jetbrains.compose.resources.stringResource

@Composable
fun FolderActionsMenu(
    menuAvailable: Boolean,
    onRename: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val tokens = designTokens()
    var menuExpanded by remember { mutableStateOf(false) }

    val menuShape = RoundedCornerShape(tokens.radius.lg)
    val menuContainerColor = tokens.colors.elevatedSurface
    val menuBorderColor = tokens.colors.divider.copy(alpha = 0.35f)

    Box(modifier = modifier) {
        IconButton(
            onClick = { if (menuAvailable) menuExpanded = true },
            enabled = menuAvailable,
            modifier = Modifier.size(tokens.spacing.xxl),
        ) {
            val tint = if (menuAvailable) {
                tokens.colors.onSurface
            } else {
                tokens.colors.muted.copy(alpha = 0.4f)
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
                    .shadow(tokens.elevation.popover, menuShape, clip = false)
                    .clip(menuShape)
                    .background(menuContainerColor)
                    .border(BorderStroke(androidx.compose.ui.unit.Dp.Hairline, menuBorderColor), menuShape),
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
                                tint = tokens.colors.muted,
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            it()
                        },
                        contentPadding = PaddingValues(
                            horizontal = tokens.spacing.xl,
                            vertical = tokens.spacing.md,
                        ),
                    )
                }

                if (onRename != null && onDelete != null) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = tokens.spacing.md),
                        color = menuBorderColor.copy(alpha = 0.5f),
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
                        contentPadding = PaddingValues(
                            horizontal = tokens.spacing.xl,
                            vertical = tokens.spacing.md,
                        ),
                    )
                }
            }
        }
    }
}

@DevicePreviews
@Composable
private fun FolderActionsMenuPreview() {
    DevicePreviewContainer {
        FolderActionsMenu(menuAvailable = true, onRename = {}, onDelete = {})
    }
}
