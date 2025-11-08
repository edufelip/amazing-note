package com.edufelip.shared.ui.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.edufelip.shared.domain.model.Caret
import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.NoteBlock
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.TextBlock
import com.edufelip.shared.domain.model.insertImageAtCaret

class NoteEditorState internal constructor(initialContent: NoteContent) {
    internal val blockList = mutableStateListOf<NoteBlock>()
    private val documentHandle = EditorDocument(blockList)
    val document: EditorDocument
        get() = documentHandle
    private val textFieldValues = mutableStateMapOf<String, TextFieldValue>()
    var caret: Caret? by mutableStateOf(null)
        private set
    var docSelection: DocSelection? by mutableStateOf(null)
        private set
    var focusedBlockId: String? by mutableStateOf(null)
        private set
    internal var pendingFocusId: String? by mutableStateOf(null)
        private set
    var selectedImageBlockId: String? by mutableStateOf(null)
        private set
    var canUndo by mutableStateOf(false)
        private set
    var canRedo by mutableStateOf(false)
        private set
    private val undoStack = ArrayDeque<EditorSnapshot>()
    private val redoStack = ArrayDeque<EditorSnapshot>()

    init {
        setContent(initialContent)
    }

    val content: NoteContent
        get() = NoteContent(blockList.toList())

    fun textFieldValueFor(block: TextBlock): TextFieldValue {
        val existing = textFieldValues[block.id]
        if (existing == null) {
            val created = TextFieldValue(block.text, TextRange(block.text.length))
            textFieldValues[block.id] = created
            return created
        }
        if (existing.text != block.text) {
            val selection = existing.selection.clampTo(block.text.length)
            val updated = existing.copy(text = block.text, selection = selection)
            textFieldValues[block.id] = updated
            return updated
        }
        return existing
    }

    fun onTextFieldValueChange(blockId: String, newValue: TextFieldValue) {
        val current = textFieldValues[blockId]
        if (current == newValue) return
        textFieldValues[blockId] = newValue
        onTextChanged(blockId, newValue.text)
        updateCaret(blockId, newValue.selection.start, newValue.selection.end)
    }

    fun setContent(newContent: NoteContent) {
        if (newContent.blocks.isEmpty()) {
            val existingText = blockList.singleOrNull() as? TextBlock
            if (existingText != null && existingText.text.isEmpty() && existingText.spans.isEmpty()) {
                return
            }
        }
        val normalized = newContent.normalizedBlocks()
        if (blockList.sameAs(normalized)) return
        blockList.clear()
        blockList.addAll(normalized)
        refreshTextFieldState()
        ensureSelectedImageIsValid()
        ensureCaretWithinBounds()
    }

    fun onTextChanged(blockId: String, updatedText: String) {
        val index = blockList.indexOfFirst { it.id == blockId }
        if (index < 0) return
        val block = blockList[index]
        if (block is TextBlock && block.text != updatedText) {
            pushUndoSnapshot()
            blockList[index] = block.copy(text = updatedText)
            textFieldValues[blockId]?.let { current ->
                if (current.text != updatedText) {
                    textFieldValues[blockId] = current.copy(
                        text = updatedText,
                        selection = current.selection.clampTo(updatedText.length),
                    )
                }
            }
        }
    }

    fun updateCaret(blockId: String, selectionStart: Int, selectionEnd: Int) {
        setCaretFrom(Caret(blockId, selectionStart, selectionEnd))
    }

    fun markFocus(blockId: String) {
        focusedBlockId = blockId
        clearImageSelection()
        if (pendingFocusId == blockId) {
            pendingFocusId = null
        }
        if (caret?.blockId != blockId) {
            val block = blockList.firstOrNull { it.id == blockId }
            if (block is TextBlock) {
                setCaretFrom(Caret(blockId, block.text.length))
            }
        }
    }

    fun requestFocus(blockId: String) {
        pendingFocusId = blockId
    }

    fun consumePendingFocus(blockId: String) {
        if (pendingFocusId == blockId) {
            pendingFocusId = null
        }
    }

    fun focusFirstTextBlock() {
        val first = blockList.firstOrNull { it is TextBlock } as? TextBlock ?: return
        requestFocus(first.id)
    }

    fun focusTextAdjacentTo(blockId: String) {
        val index = blockList.indexOfFirst { it.id == blockId }
        if (index == -1) return
        val after = blockList.subList(index + 1, blockList.size).firstOrNull { it is TextBlock } as? TextBlock
        val before = blockList.subList(0, index).lastOrNull { it is TextBlock } as? TextBlock
        val target = after ?: before ?: blockList.firstOrNull { it is TextBlock } as? TextBlock ?: return
        requestFocus(target.id)
    }

    fun toggleImageSelection(blockId: String) {
        selectedImageBlockId = if (selectedImageBlockId == blockId) {
            null
        } else {
            focusTextAdjacentTo(blockId)
            blockId
        }
    }

    fun clearImageSelection() {
        selectedImageBlockId = null
    }

    fun isImageSelected(blockId: String): Boolean = selectedImageBlockId == blockId

    fun insertImageAtCaret(
        uri: String,
        width: Int? = null,
        height: Int? = null,
        alt: String? = null,
        mimeType: String? = null,
        fileName: String? = null,
        thumbnailUri: String? = null,
    ): Caret? {
        val snapshot = caret ?: defaultCaret()
        pushUndoSnapshot()
        val result = insertImageAtCaret(
            content = content,
            caret = snapshot,
            imageBlock = ImageBlock(
                uri = uri,
                width = width,
                height = height,
                alt = alt,
                mimeType = mimeType,
                fileName = fileName,
                thumbnailUri = thumbnailUri,
            ),
        )
        blockList.clear()
        blockList.addAll(result.content.normalizedBlocks())
        refreshTextFieldState()
        setCaretFrom(result.nextCaret)
        focusedBlockId = result.nextCaret.blockId
        clearImageSelection()
        ensureSelectedImageIsValid()
        ensureTrailingBlankLine()
        return caret
    }

    fun moveCaretToEnd() {
        val lastText = blockList.lastOrNull { it is TextBlock } as? TextBlock ?: return
        val end = lastText.text.length
        setCaretFrom(Caret(lastText.id, end))
        focusedBlockId = lastText.id
        requestFocus(lastText.id)
    }

    fun removeBlockById(blockId: String): Boolean {
        val index = blockList.indexOfFirst { it.id == blockId }
        if (index < 0) return false
        val removed = blockList[index]
        pushUndoSnapshot()
        blockList.removeAt(index)
        if (blockList.none { it is TextBlock }) {
            blockList.add(TextBlock(text = ""))
        }
        if (removed is TextBlock) {
            textFieldValues.remove(removed.id)
        }
        refreshTextFieldState()
        if (selectedImageBlockId == blockId) {
            selectedImageBlockId = null
        }
        ensureSelectedImageIsValid()
        ensureCaretWithinBounds()
        return true
    }

    fun consumeSelectedImageBeforeTextInput(): Boolean {
        val selectedId = selectedImageBlockId ?: return false
        return removeBlockById(selectedId)
    }

    fun removeSelectedImage(): Boolean = consumeSelectedImageBeforeTextInput()

    fun removeImageBefore(blockId: String): Boolean {
        val index = blockList.indexOfFirst { it.id == blockId }
        if (index <= 0) return false
        val previous = blockList[index - 1]
        return if (previous is ImageBlock) {
            removeBlockById(previous.id)
        } else {
            false
        }
    }

    private fun ensureCaretWithinBounds() {
        val current = caret ?: return
        val block = blockList.firstOrNull { it.id == current.blockId }
        if (block is TextBlock) {
            val end = block.text.length
            val clampedStart = current.start.coerceIn(0, end)
            val clampedEnd = current.end.coerceIn(0, end)
            setCaretFrom(Caret(current.blockId, clampedStart, clampedEnd))
        } else {
            setCaretFrom(defaultCaret())
        }
    }

    private fun defaultCaret(): Caret {
        val existing = blockList.lastOrNull { it is TextBlock } as? TextBlock
        val target = existing ?: TextBlock(text = "").also {
            blockList += it
            refreshTextFieldState()
        }
        return Caret(target.id, target.text.length)
    }

    private fun ensureSelectedImageIsValid() {
        val selectedId = selectedImageBlockId ?: return
        if (blockList.none { it.id == selectedId }) {
            selectedImageBlockId = null
        }
    }

    private fun ensureTrailingBlankLine() {
        val lastIndex = blockList.indexOfLast { it is TextBlock }
        if (lastIndex == -1) return
        val block = blockList[lastIndex] as TextBlock
        if (!block.text.endsWith("\n")) {
            val updated = block.copy(text = block.text + "\n")
            blockList[lastIndex] = updated
            refreshTextFieldState()
            setCaretFrom(Caret(updated.id, updated.text.length))
            focusedBlockId = updated.id
            requestFocus(updated.id)
        }
    }

    private fun refreshTextFieldState() {
        val textBlocks = blockList.filterIsInstance<TextBlock>()
        val validIds = textBlocks.map { it.id }.toSet()
        val iterator = textFieldValues.keys.iterator()
        while (iterator.hasNext()) {
            val id = iterator.next()
            if (id !in validIds) {
                iterator.remove()
            }
        }
        textBlocks.forEach { block ->
            val existing = textFieldValues[block.id]
            if (existing == null) {
                textFieldValues[block.id] = TextFieldValue(block.text, TextRange(block.text.length))
            } else if (existing.text != block.text) {
                textFieldValues[block.id] = existing.copy(
                    text = block.text,
                    selection = existing.selection.clampTo(block.text.length),
                )
            }
        }
    }

    private fun setCaretFrom(newCaret: Caret?) {
        caret = newCaret
        docSelection = newCaret?.let {
            DocSelection(
                anchor = BlockCursor(it.blockId, it.start),
                focus = BlockCursor(it.blockId, it.end),
            )
        }
    }

    fun undo(): Boolean {
        val previous = undoStack.removeLastOrNull() ?: return false
        redoStack.addLast(captureSnapshot())
        trimStackIfNeeded(redoStack)
        applySnapshot(previous)
        updateHistoryFlags()
        return true
    }

    fun redo(): Boolean {
        val snapshot = redoStack.removeLastOrNull() ?: return false
        undoStack.addLast(captureSnapshot())
        trimStackIfNeeded(undoStack)
        applySnapshot(snapshot)
        updateHistoryFlags()
        return true
    }

    private fun pushUndoSnapshot() {
        undoStack.addLast(captureSnapshot())
        trimStackIfNeeded(undoStack)
        redoStack.clear()
        updateHistoryFlags()
    }

    private fun captureSnapshot(): EditorSnapshot = EditorSnapshot(
        content = content,
        caret = caret,
        focusedBlockId = focusedBlockId,
    )

    private fun applySnapshot(snapshot: EditorSnapshot) {
        blockList.clear()
        blockList.addAll(snapshot.content.normalizedBlocks())
        refreshTextFieldState()
        setCaretFrom(snapshot.caret)
        focusedBlockId = snapshot.focusedBlockId
        pendingFocusId = snapshot.focusedBlockId
        clearImageSelection()
        ensureSelectedImageIsValid()
        ensureCaretWithinBounds()
    }

    private fun trimStackIfNeeded(stack: ArrayDeque<EditorSnapshot>) {
        while (stack.size > MAX_HISTORY) {
            stack.removeFirst()
        }
    }

    private fun updateHistoryFlags() {
        canUndo = undoStack.isNotEmpty()
        canRedo = redoStack.isNotEmpty()
    }

    private data class EditorSnapshot(
        val content: NoteContent,
        val caret: Caret?,
        val focusedBlockId: String?,
    )

    private companion object {
        private const val MAX_HISTORY = 20
    }
}

@Composable
fun rememberNoteEditorState(
    noteKey: Any?,
    initialContent: NoteContent,
    onContentChanged: (NoteContent) -> Unit = {},
): NoteEditorState {
    val state = remember(noteKey) { NoteEditorState(initialContent) }
    val latestContent by rememberUpdatedState(initialContent)
    LaunchedEffect(noteKey, latestContent) {
        state.setContent(latestContent)
    }
    val latestCallback by rememberUpdatedState(onContentChanged)
    LaunchedEffect(state) {
        snapshotFlow { state.content }
            .collect { latestCallback(it) }
    }
    return state
}

private fun NoteContent.normalizedBlocks(): List<NoteBlock> {
    val sanitized = blocks.ifEmpty { listOf(TextBlock(text = "")) }
    val last = sanitized.lastOrNull()
    return if (last is TextBlock) sanitized else sanitized + TextBlock(text = "")
}

private fun List<NoteBlock>.sameAs(other: List<NoteBlock>): Boolean {
    if (size != other.size) return false
    for (i in indices) {
        val a = this[i]
        val b = other[i]
        if (a::class != b::class) return false
        when (a) {
            is TextBlock -> {
                val otherText = b as? TextBlock ?: return false
                if (a.id != otherText.id || a.text != otherText.text || a.spans != otherText.spans) return false
            }

            is ImageBlock -> {
                val otherImage = b as? ImageBlock ?: return false
                if (a.id != otherImage.id || a.uri != otherImage.uri || a.width != otherImage.width || a.height != otherImage.height || a.alt != otherImage.alt) return false
            }
        }
    }
    return true
}

private fun TextRange.clampTo(maxLength: Int): TextRange {
    val newStart = start.coerceIn(0, maxLength)
    val newEnd = end.coerceIn(newStart, maxLength)
    return if (newStart == start && newEnd == end) this else TextRange(newStart, newEnd)
}
