package com.edufelip.shared.ui.screens

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import com.edufelip.shared.ui.preview.ScreenPreviewsDarkLight
import com.edufelip.shared.ui.theme.AmazingNoteTheme

private fun sampleNotes(): List<Note> = List(10) { index ->
    Note(
        id = index + 1,
        title = "Note #${index + 1}",
        priority = when (index % 3) {
            0 -> Priority.HIGH
            1 -> Priority.MEDIUM
            else -> Priority.LOW
        },
        description = "This is a sample note to preview different layouts and sizes.",
        deleted = false
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ScreenPreviewsDarkLight
@Composable
fun ListScreen_Previews() {
    AmazingNoteTheme {
        ListScreen(
            notes = sampleNotes(),
            onNoteClick = {},
            onAddClick = {},
            searchQuery = "",
            onSearchQueryChange = {},
            drawerState = rememberDrawerState(DrawerValue.Closed),
            drawerContent = null,
            onDelete = {}
        )
    }
}

