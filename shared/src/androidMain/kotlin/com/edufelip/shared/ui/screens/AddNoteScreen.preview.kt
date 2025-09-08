package com.edufelip.shared.ui.screens

import androidx.compose.runtime.Composable
import com.edufelip.shared.model.Priority
import com.edufelip.shared.ui.preview.ScreenPreviewsDarkLight
import com.edufelip.shared.ui.theme.AmazingNoteTheme

@ScreenPreviewsDarkLight
@Composable
fun AddNoteScreen_Previews() {
    AmazingNoteTheme {
        AddNoteScreen(
            title = "New Note",
            onTitleChange = {},
            priority = Priority.MEDIUM,
            onPriorityChange = {},
            description = "Describe your note here...",
            onDescriptionChange = {},
            onBack = {},
            onSave = {},
            onDelete = null
        )
    }
}

