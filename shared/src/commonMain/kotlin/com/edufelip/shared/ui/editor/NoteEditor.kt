package com.edufelip.shared.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.TextBlock

@Composable
fun NoteEditor(
    state: NoteEditorState,
    modifier: Modifier = Modifier,
    placeholder: String = "",
) {
    val contentAwareModifier = modifier.noteEditorReceiveContent { uri ->
        state.insertImageAtCaret(uri = uri)
    }
    val blocks = state.blockList
    val firstTextBlockId = blocks.firstOrNull { it is TextBlock }?.id
    LazyColumn(
        modifier = contentAwareModifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(blocks, key = { it.id }) { block ->
            when (block) {
                is TextBlock -> TextBlockEditor(
                    block = block,
                    state = state,
                    placeholder = placeholder,
                    showPlaceholder = block.id == firstTextBlockId,
                )
                is ImageBlock -> ImageBlockView(block)
            }
        }
    }
}

@Composable
private fun TextBlockEditor(
    block: TextBlock,
    state: NoteEditorState,
    modifier: Modifier = Modifier,
    placeholder: String,
    showPlaceholder: Boolean,
) {
    var value by remember(block.id) { mutableStateOf(TextFieldValue(block.text, TextRange(block.text.length))) }
    LaunchedEffect(block.id, block.text) {
        if (value.text != block.text) {
            value = TextFieldValue(block.text, TextRange(block.text.length))
        }
    }

    val typography = MaterialTheme.typography.bodyLarge
    val textColor = MaterialTheme.colorScheme.onSurface

    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            value = newValue
            state.onTextChanged(block.id, newValue.text)
            state.updateCaret(block.id, newValue.selection.start, newValue.selection.end)
        },
        textStyle = typography.copy(color = textColor),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    state.markFocus(block.id)
                }
            },
        decorationBox = { innerField ->
            Box(modifier = Modifier.fillMaxWidth()) {
                if (showPlaceholder && value.text.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = typography,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                innerField()
            }
        },
    )
}

@Composable
private fun ImageBlockView(block: ImageBlock) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp,
    ) {
        AsyncImage(
            model = block.remoteUri ?: block.uri,
            contentDescription = block.alt,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
