package com.edufelip.amazing_note.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.edufelip.amazing_note.R
import com.edufelip.amazing_note.data.models.Note
import com.edufelip.amazing_note.data.models.Priority
import com.edufelip.amazing_note.ui.gadgets.NoteRow

@ExperimentalMaterial3Api
@Composable
fun TrashScreen(
    notes: List<Note>,
    onBack: () -> Unit,
    onNoteClick: (Note) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trash") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painter = painterResource(id = R.drawable.ic_arrow_left), contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)) {
                items(notes, key = { it.id }) { note ->
                    NoteRow(
                        note = note,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        onClick = onNoteClick
                    )
                }
            }
        }
    }
}

@Composable
fun TrashNoteXmlScreen(
    title: String,
    priority: Priority,
    description: String,
    onBack: () -> Unit
) {
    UpdateNoteScreen(
        title = title,
        onTitleChange = {},
        priority = priority,
        onPriorityChange = {},
        description = description,
        onDescriptionChange = {},
        onBack = onBack,
        onSave = {},
        readOnly = true
    )
}
