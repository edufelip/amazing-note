package com.edufelip.shared.ui.editor

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edufelip.shared.domain.model.TextBlock

/**
 * Conservative note editor used on Apple platforms.
 * Uses a single TextField for content to avoid the richer editor plumbing.
 */
@Composable
fun SimpleIosNoteEditor(
    editorState: NoteEditorState,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val textBlock = editorState.blockList.firstOrNull { it is TextBlock } as? TextBlock ?: return
    val value = editorState.textFieldValueFor(textBlock)
    TextField(
        value = value,
        onValueChange = { newValue ->
            editorState.onTextFieldValueChange(textBlock.id, newValue)
        },
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(text = placeholder) },
        minLines = 6,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
        ),
    )
}
