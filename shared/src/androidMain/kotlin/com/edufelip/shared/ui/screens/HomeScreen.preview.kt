package com.edufelip.shared.ui.screens

import androidx.compose.runtime.Composable
import com.edufelip.shared.model.Folder
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import com.edufelip.shared.ui.nav.screens.HomeScreen
import com.edufelip.shared.ui.preview.PreviewLocalized
import com.edufelip.shared.ui.preview.ScreenPreviewsDarkLight

private fun placeholderNotes(): List<Note> = List(4) { index ->
    Note(
        id = index + 1,
        title = "Pinned idea #${index + 1}",
        description = "Sample note body to showcase how the list renders in previews.",
        priority = when (index % 3) {
            0 -> Priority.HIGH
            1 -> Priority.MEDIUM
            else -> Priority.LOW
        },
        createdAt = 1_700_000_000_000L + index * 3_600_000L,
        updatedAt = 1_700_000_000_000L + index * 5_400_000L,
        deleted = false,
        dirty = false,
        localUpdatedAt = 1_700_000_000_000L + index * 5_400_000L,
        folderId = if (index % 2 == 0) 1L else 2L,
    )
}

private fun placeholderFolders(): List<Folder> = listOf(
    Folder(id = 1L, name = "Personal", createdAt = 1_699_000_000_000L, updatedAt = 1_699_500_000_000L),
    Folder(id = 2L, name = "Work", createdAt = 1_699_100_000_000L, updatedAt = 1_699_600_000_000L),
)

@ScreenPreviewsDarkLight
@Composable
fun HomeScreen_EmptyPreview() {
    PreviewLocalized {
        HomeScreen(
            notes = emptyList(),
            folders = emptyList(),
            auth = null,
            onOpenNote = {},
            onAdd = {},
            onDelete = {},
            onOpenFolder = {},
            onOpenUnassigned = {},
            onOpenFolders = {},
            onCreateFolder = {},
        )
    }
}

@ScreenPreviewsDarkLight
@Composable
fun HomeScreen_PopulatedPreview() {
    PreviewLocalized {
        HomeScreen(
            notes = placeholderNotes(),
            folders = placeholderFolders(),
            auth = null,
            onOpenNote = {},
            onAdd = {},
            onDelete = {},
            onOpenFolder = {},
            onOpenUnassigned = {},
            onOpenFolders = {},
            onCreateFolder = {},
        )
    }
}
