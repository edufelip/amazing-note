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
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_more
import com.edufelip.shared.resources.folder_options_delete
import com.edufelip.shared.resources.folder_options_rename
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun FolderActionsMenu(
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

@Preview
@Composable
private fun FolderActionsMenuPreview() {
    FolderActionsMenu(menuAvailable = true, onRename = {}, onDelete = {})
}
