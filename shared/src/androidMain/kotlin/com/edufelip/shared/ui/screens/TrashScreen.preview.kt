package com.edufelip.shared.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import com.edufelip.shared.ui.preview.PreviewLocalized
import com.edufelip.shared.ui.preview.ScreenPreviewsDarkLight

private fun sampleNotes(): List<Note> = List(6) { index ->
    Note(
        id = index + 100,
        title = "Trash #${index + 1}",
        priority = when (index % 3) {
            0 -> Priority.HIGH
            1 -> Priority.MEDIUM
            else -> Priority.LOW
        },
        description = "Deleted note sample for preview.",
        deleted = true,
        createdAt = 1_700_100_000_000L + index * 3_600_000L,
        updatedAt = 1_700_100_000_000L + index * 3_600_000L,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ScreenPreviewsDarkLight
@Composable
fun TrashScreen_Previews() {
    PreviewLocalized {
        TrashScreen(
            notes = sampleNotes(),
            onRestore = {},
        )
    }
}
