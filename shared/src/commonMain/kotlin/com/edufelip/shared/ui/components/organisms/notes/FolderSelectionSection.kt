package com.edufelip.shared.ui.components.organisms.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.folder_field_label
import com.edufelip.shared.ui.components.molecules.notes.FolderPicker
import org.jetbrains.compose.resources.stringResource

@Composable
fun FolderSelectionSection(
    folders: List<Folder>,
    selectedFolderId: Long?,
    onFolderChange: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(Res.string.folder_field_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FolderPicker(
            folders = folders,
            selectedFolderId = selectedFolderId,
            onFolderChange = onFolderChange,
        )
    }
}

@Preview
@Composable
private fun FolderSelectionSectionPreview() {
    FolderSelectionSection(
        folders = listOf(
            Folder(id = 1, name = "Personal", createdAt = 0L, updatedAt = 0L),
            Folder(id = 2, name = "Work", createdAt = 0L, updatedAt = 0L),
        ),
        selectedFolderId = 2L,
        onFolderChange = {},
    )
}
