package com.edufelip.shared.ui.screens

import androidx.compose.runtime.Composable
import com.edufelip.shared.model.Note
import com.edufelip.shared.ui.nav.screens.HomeScreen
import com.edufelip.shared.ui.preview.PreviewLocalized
import com.edufelip.shared.ui.preview.ScreenPreviewsDarkLight

private fun placeholderNotes(): List<Note> = List(4) { index ->
    Note(
        id = index + 1,
        title = "Pinned idea #${index + 1}",
        description = "Sample note body to showcase how the list renders in previews.",
        deleted = false,
        createdAt = 1_700_000_000_000L + index * 3_600_000L,
        updatedAt = 1_700_000_000_000L + index * 5_400_000L,
        dirty = false,
        localUpdatedAt = 1_700_000_000_000L + index * 5_400_000L,
        folderId = if (index % 2 == 0) 1L else 2L,
    )
}

@ScreenPreviewsDarkLight
@Composable
fun HomeScreen_EmptyPreview() {
    PreviewLocalized {
        HomeScreen(
            notes = emptyList(),
            auth = null,
            onOpenNote = {},
            onAdd = {},
            onDelete = {},
        )
    }
}

@ScreenPreviewsDarkLight
@Composable
fun HomeScreen_PopulatedPreview() {
    PreviewLocalized {
        HomeScreen(
            notes = placeholderNotes(),
            auth = null,
            onOpenNote = {},
            onAdd = {},
            onDelete = {},
        )
    }
}
