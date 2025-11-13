package com.edufelip.shared.ui.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.ImageSyncState
import com.edufelip.shared.domain.model.TextBlock
import com.edufelip.shared.ui.designsystem.designTokens

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteEditor(
    state: NoteEditorState,
    modifier: Modifier = Modifier,
    placeholder: String = "",
) {
    val contentAwareModifier = modifier
        .noteEditorReceiveContent { uri ->
            val localUri = uri.takeUnless { isRemoteUri(it) }
            state.insertImageAtCaret(
                uri = uri,
                localUri = localUri,
                syncState = ImageSyncState.PendingUpload,
            )
        }
        .pointerInput(state) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                if (down.isConsumed) return@awaitEachGesture
                val up = waitForUpOrCancellation()
                if (up == null || up.isConsumed) return@awaitEachGesture
                state.clearImageSelection()
                state.focusFirstTextBlock()
            }
        }
    val document = state.document
    val firstTextBlockId = document.firstOrNull { it is TextBlock }?.id
    LazyColumn(
        modifier = contentAwareModifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        items(
            items = document,
            key = { it.id },
        ) { block ->
            when (block) {
                is TextBlock -> TextBlockEditor(
                    block = block,
                    state = state,
                    placeholder = placeholder,
                    showPlaceholder = block.id == firstTextBlockId,
                )

                is ImageBlock -> ImageBlockView(
                    block = block,
                    selected = state.isImageSelected(block.id),
                    onSelect = { state.toggleImageSelection(block.id) },
                    onMove = { id, delta -> state.moveBlockBy(id, delta) },
                )
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
    val value = state.textFieldValueFor(block)
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var pendingBringIntoView by remember { mutableStateOf(false) }
    LaunchedEffect(pendingBringIntoView) {
        if (pendingBringIntoView) {
            bringIntoViewRequester.bringIntoView()
            pendingBringIntoView = false
        }
    }
    LaunchedEffect(state.pendingFocusId, block.id) {
        if (state.pendingFocusId == block.id) {
            focusRequester.requestFocus()
            state.consumePendingFocus(block.id)
        }
    }

    val typography = MaterialTheme.typography.bodyLarge
    val textColor = MaterialTheme.colorScheme.onSurface

    BasicTextField(
        value = value,
        onValueChange = { newValue ->
            state.consumeSelectedImageBeforeTextInput()
            state.onTextFieldValueChange(block.id, newValue)
        },
        textStyle = typography.copy(color = textColor),
        modifier = modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .bringIntoViewRequester(bringIntoViewRequester)
            .padding(horizontal = 4.dp)
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    when {
                        event.isCopyShortcut() -> {
                            if (state.copySelectedBlocks()) {
                                return@onPreviewKeyEvent true
                            }
                        }

                        event.isCutShortcut() -> {
                            if (state.cutSelectedBlocks()) {
                                return@onPreviewKeyEvent true
                            }
                        }

                        event.isUndoShortcut() -> {
                            if (state.undo()) {
                                return@onPreviewKeyEvent true
                            }
                        }

                        event.isRedoShortcut() -> {
                            if (state.redo()) {
                                return@onPreviewKeyEvent true
                            }
                        }

                        event.isPasteShortcut() -> {
                            if (state.pasteBlocks()) {
                                return@onPreviewKeyEvent true
                            }
                        }
                    }
                    val pressedKey = event.key
                    if (state.selectedImageBlockId != null) {
                        val removed = state.removeSelectedImage()
                        if (pressedKey == Key.Backspace || pressedKey == Key.Delete) {
                            return@onPreviewKeyEvent removed
                        }
                        return@onPreviewKeyEvent false
                    }
                    if (pressedKey == Key.Backspace || pressedKey == Key.Delete) {
                        val selection = value.selection
                        val collapsedAtStart = selection.start == selection.end && selection.start == 0
                        if (collapsedAtStart && state.removeImageBefore(block.id)) {
                            return@onPreviewKeyEvent true
                        }
                    }
                }
                false
            }
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    state.consumePendingFocus(block.id)
                    state.markFocus(block.id)
                    state.clearImageSelection()
                    pendingBringIntoView = true
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
private fun ImageBlockView(
    block: ImageBlock,
    selected: Boolean,
    onSelect: () -> Unit,
    onMove: (String, Int) -> Unit,
) {
    val tokens = designTokens()
    val dragThreshold = with(LocalDensity.current) { 36.dp.toPx() }
    var dragDelta by remember(block.id) { mutableStateOf(0f) }
    var dragging by remember(block.id) { mutableStateOf(false) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .clickable(onClick = onSelect)
            .pointerInput(block.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        dragging = true
                        dragDelta = 0f
                    },
                    onDragCancel = {
                        dragging = false
                        dragDelta = 0f
                    },
                    onDragEnd = {
                        dragging = false
                        dragDelta = 0f
                    },
                    onDrag = { change, amount ->
                        dragDelta += amount.y
                        when {
                            dragDelta <= -dragThreshold -> {
                                onMove(block.id, -1)
                                dragDelta += dragThreshold
                            }

                            dragDelta >= dragThreshold -> {
                                onMove(block.id, 1)
                                dragDelta -= dragThreshold
                            }
                        }
                    },
                )
            },
        shape = RoundedCornerShape(18.dp),
        tonalElevation = if (dragging) 4.dp else 1.dp,
        border = when {
            dragging -> BorderStroke(2.dp, tokens.colors.accent)
            selected -> BorderStroke(2.dp, tokens.colors.accent)
            else -> null
        },
    ) {
        AsyncImage(
            model = block.localUri ?: block.uri,
            contentDescription = block.alt,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun KeyEvent.isCopyShortcut(): Boolean = isShortcut(Key.C)

private fun KeyEvent.isCutShortcut(): Boolean = isShortcut(Key.X)

private fun KeyEvent.isPasteShortcut(): Boolean = isShortcut(Key.V)

private fun KeyEvent.isUndoShortcut(): Boolean = isShortcut(Key.Z) && !isShiftPressed

private fun KeyEvent.isRedoShortcut(): Boolean = (isShortcut(Key.Z) && isShiftPressed) || isShortcut(Key.Y)

private fun KeyEvent.isShortcut(targetKey: Key): Boolean = type == KeyEventType.KeyDown && (isCtrlPressed || isMetaPressed) && key == targetKey

private fun isRemoteUri(uri: String): Boolean = uri.startsWith("http", ignoreCase = true)
