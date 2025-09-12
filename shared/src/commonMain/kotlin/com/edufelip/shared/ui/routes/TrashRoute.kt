package com.edufelip.shared.ui.routes

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import com.edufelip.shared.model.Note
import com.edufelip.shared.ui.screens.TrashScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashRoute(
    notes: List<Note>,
    onRestore: (Note) -> Unit,
) {
    TrashScreen(
        notes = notes,
        onRestore = onRestore,
    )
}
