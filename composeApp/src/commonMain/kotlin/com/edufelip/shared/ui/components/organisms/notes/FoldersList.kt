package com.edufelip.shared.ui.components.organisms.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.ui.components.molecules.notes.FolderListCard
import com.edufelip.shared.ui.components.molecules.notes.folderCountLabel
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews

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
    val tokens = designTokens()
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = tokens.spacing.xl,
            vertical = tokens.spacing.lg,
        ),
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
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

@DevicePreviews
@Composable
private fun FoldersListPreview() {
    DevicePreviewContainer {
        val folders = listOf(
            Folder(id = 1, name = "Personal", createdAt = 0, updatedAt = 0),
            Folder(id = 2, name = "Work", createdAt = 0, updatedAt = 0),
        )
        val tokens = designTokens()
        val palette = listOf(tokens.colors.accent, tokens.colors.accentMuted)
        FoldersList(
            folders = folders,
            notesByFolder = mapOf(1L to 7, 2L to 3),
            accentPalette = palette,
            onOpenFolder = {},
            onRequestRename = {},
            onRequestDelete = {},
        )
    }
}
