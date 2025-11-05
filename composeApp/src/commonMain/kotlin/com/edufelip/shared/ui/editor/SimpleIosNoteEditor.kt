package com.edufelip.shared.ui.editor

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
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
    var value by remember(textBlock.id) {
        mutableStateOf(TextFieldValue(textBlock.text, TextRange(textBlock.text.length)))
    }
    LaunchedEffect(textBlock.id, textBlock.text) {
        if (value.text != textBlock.text) {
            value = TextFieldValue(textBlock.text, TextRange(textBlock.text.length))
        }
    }
    TextField(
        value = value,
        onValueChange = { newValue ->
            value = newValue
            editorState.onTextChanged(textBlock.id, newValue.text)
            editorState.updateCaret(textBlock.id, newValue.selection.start, newValue.selection.end)
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
