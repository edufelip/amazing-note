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
import com.edufelip.shared.preview.Preview
import androidx.compose.ui.unit.dp
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.ui.components.molecules.notes.FolderGridCard
import com.edufelip.shared.ui.components.molecules.notes.folderCountLabel

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

@Preview
@Composable
private fun FoldersGridPreview() {
    val folders = listOf(
        Folder(id = 1, name = "Personal", createdAt = 0, updatedAt = 0),
        Folder(id = 2, name = "Work", createdAt = 0, updatedAt = 0),
    )
    val palette = listOf(Color(0xFF4E6CEF), Color(0xFF00897B))
    FoldersGrid(
        folders = folders,
        notesByFolder = mapOf(1L to 7, 2L to 3),
        accentPalette = palette,
        onOpenFolder = {},
        onRequestRename = {},
        onRequestDelete = {},
    )
}
