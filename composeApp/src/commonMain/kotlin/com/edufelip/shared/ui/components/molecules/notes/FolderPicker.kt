package com.edufelip.shared.ui.components.molecules.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.no_folder_label
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import org.jetbrains.compose.resources.stringResource

@Composable
fun FolderPicker(
    folders: List<Folder>,
    selectedFolderId: Long?,
    onFolderChange: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val expanded = remember { mutableStateOf(false) }
    val ordered = remember(folders) { folders.sortedBy { it.name } }
    val label = selectedFolderId?.let { id -> ordered.firstOrNull { it.id == id }?.name }
        ?: stringResource(Res.string.no_folder_label)
    val tokens = designTokens()
    val shape = RoundedCornerShape(tokens.radius.lg + tokens.radius.sm)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded.value = true },
        shape = shape,
        color = tokens.colors.elevatedSurface.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = tokens.spacing.xl,
                    vertical = tokens.spacing.md,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        ) {
            Icon(
                imageVector = Icons.Outlined.Folder,
                contentDescription = null,
                tint = tokens.colors.muted,
            )
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    DropdownMenu(
        expanded = expanded.value,
        onDismissRequest = { expanded.value = false },
        modifier = Modifier.widthIn(min = 240.dp),
    ) {
        DropdownMenuItem(
            modifier = Modifier.fillMaxWidth(),
            text = {
                Text(
                    text = stringResource(Res.string.no_folder_label),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Folder,
                    contentDescription = null,
                    tint = tokens.colors.muted,
                )
            },
            onClick = {
                onFolderChange(null)
                expanded.value = false
            },
        )
        ordered.forEach { folder ->
            DropdownMenuItem(
                modifier = Modifier.fillMaxWidth(),
                text = {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Folder,
                        contentDescription = null,
                        tint = tokens.colors.muted,
                    )
                },
                onClick = {
                    onFolderChange(folder.id)
                    expanded.value = false
                },
            )
        }
    }
}

@DevicePreviews
@Composable
private fun FolderPickerPreview() {
    val folders = listOf(
        Folder(id = 1, name = "Personal", createdAt = 0L, updatedAt = 0L),
        Folder(id = 2, name = "Work", createdAt = 0L, updatedAt = 0L),
    )
    DevicePreviewContainer {
        FolderPicker(
            folders = folders,
            selectedFolderId = 1L,
            onFolderChange = {},
        )
    }
}
