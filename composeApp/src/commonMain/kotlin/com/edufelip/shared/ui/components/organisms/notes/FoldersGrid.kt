package com.edufelip.shared.ui.components.organisms.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.ui.components.molecules.notes.FolderGridCard
import com.edufelip.shared.ui.components.molecules.notes.folderCountLabel
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews

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
    val tokens = designTokens()
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(
            horizontal = tokens.spacing.xl,
            vertical = tokens.spacing.lg,
        ),
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
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
                contentPadding = PaddingValues(
                    horizontal = tokens.spacing.lg,
                    vertical = tokens.spacing.md,
                ),
            )
        }
    }
}

@DevicePreviews
@Composable
private fun FoldersGridPreview() {
    DevicePreviewContainer {
        val folders = listOf(
            Folder(id = 1, name = "Personal", createdAt = 0, updatedAt = 0),
            Folder(id = 2, name = "Work", createdAt = 0, updatedAt = 0),
        )
        val tokens = designTokens()
        val palette = listOf(tokens.colors.accent, tokens.colors.accentMuted)
        FoldersGrid(
            folders = folders,
            notesByFolder = mapOf(1L to 7, 2L to 3),
            accentPalette = palette,
            onOpenFolder = {},
            onRequestRename = {},
            onRequestDelete = {},
        )
    }
}
