package com.edufelip.shared.ui.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.edufelip.shared.domain.model.Caret
import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.NoteBlock
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.TextBlock
import com.edufelip.shared.domain.model.insertImageAtCaret

class NoteEditorState internal constructor(initialContent: NoteContent) {
    internal val blockList = mutableStateListOf<NoteBlock>()
    var caret: Caret? by mutableStateOf(null)
        private set
    var focusedBlockId: String? by mutableStateOf(null)
        private set

    init {
        setContent(initialContent)
    }

    val content: NoteContent
        get() = NoteContent(blockList.toList())

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
        ensureCaretWithinBounds()
    }

    fun onTextChanged(blockId: String, updatedText: String) {
        val index = blockList.indexOfFirst { it.id == blockId }
        if (index < 0) return
        val block = blockList[index]
        if (block is TextBlock && block.text != updatedText) {
            blockList[index] = block.copy(text = updatedText)
        }
    }

    fun updateCaret(blockId: String, selectionStart: Int, selectionEnd: Int) {
        caret = Caret(blockId, selectionStart, selectionEnd)
    }

    fun markFocus(blockId: String) {
        focusedBlockId = blockId
        if (caret?.blockId != blockId) {
            val block = blockList.firstOrNull { it.id == blockId }
            if (block is TextBlock) {
                caret = Caret(blockId, block.text.length)
            }
        }
    }

    fun insertImageAtCaret(
        uri: String,
        width: Int? = null,
        height: Int? = null,
        alt: String? = null,
    ): Caret? {
        val snapshot = caret ?: defaultCaret()
        val result = insertImageAtCaret(
            content = content,
            caret = snapshot,
            imageBlock = ImageBlock(uri = uri, width = width, height = height, alt = alt),
        )
        blockList.clear()
        blockList.addAll(result.content.normalizedBlocks())
        caret = result.nextCaret
        focusedBlockId = result.nextCaret.blockId
        return caret
    }

    fun removeBlockById(blockId: String) {
        val index = blockList.indexOfFirst { it.id == blockId }
        if (index < 0) return
        blockList.removeAt(index)
        if (blockList.none { it is TextBlock }) {
            blockList.add(TextBlock(text = ""))
        }
        ensureCaretWithinBounds()
    }

    private fun ensureCaretWithinBounds() {
        val current = caret ?: return
        val block = blockList.firstOrNull { it.id == current.blockId }
        if (block is TextBlock) {
            val end = block.text.length
            val clampedStart = current.start.coerceIn(0, end)
            val clampedEnd = current.end.coerceIn(0, end)
            caret = Caret(current.blockId, clampedStart, clampedEnd)
        } else {
            caret = defaultCaret()
        }
    }

    private fun defaultCaret(): Caret {
        val lastText = blockList.lastOrNull { it is TextBlock } as? TextBlock
            ?: TextBlock(text = "").also { blockList += it }
        return Caret(lastText.id, lastText.text.length)
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
