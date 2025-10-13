package com.edufelip.shared.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.edufelip.shared.model.Note
import com.edufelip.shared.ui.nav.screens.ListScreen
import com.edufelip.shared.ui.preview.PreviewLocalized
import com.edufelip.shared.ui.preview.ScreenPreviewsDarkLight

private fun sampleNotes(): List<Note> = List(10) { index ->
    Note(
        id = index + 1,
        title = "Note #${index + 1}",
        description = "This is a sample note to preview different layouts and sizes.",
        deleted = false,
        createdAt = 1_700_000_000_000L + index * 3_600_000L,
        updatedAt = 1_700_000_000_000L + index * 3_600_000L,
        dirty = false,
        localUpdatedAt = 1_700_000_000_000L + index * 3_600_000L,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ScreenPreviewsDarkLight
@Composable
fun ListScreen_Previews() {
    PreviewLocalized {
        ListScreen(
            notes = sampleNotes(),
            onNoteClick = {},
            onAddClick = {},
            searchQuery = "",
            onSearchQueryChange = {},
            onDelete = {},
        )
    }
}
