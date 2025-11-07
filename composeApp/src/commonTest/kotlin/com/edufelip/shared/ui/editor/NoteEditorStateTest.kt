package com.edufelip.shared.ui.editor

import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.TextBlock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NoteEditorStateTest {
    @Test
    fun insertImageAdvancesCaretToTrailingBlock() {
        val initialBlock = TextBlock(text = "Hello world")
        val state = NoteEditorState(NoteContent(listOf(initialBlock)))
        state.updateCaret(initialBlock.id, 5, 5)

        val caret = state.insertImageAtCaret(
            uri = "file://local/image",
            width = 100,
            height = 200,
            alt = "Sample",
        )

        val blocks = state.blockList
        assertEquals(3, blocks.size)
        val trailing = blocks.last() as TextBlock
        assertEquals(" world", trailing.text)
        assertNotNull(caret)
        assertEquals(trailing.id, caret.blockId)
        assertEquals(0, caret.start)
    }

    @Test
    fun undoAndRedoRestoreTextChanges() {
        val initialBlock = TextBlock(text = "Hello")
        val state = NoteEditorState(NoteContent(listOf(initialBlock)))

        state.onTextChanged(initialBlock.id, "Hello World")

        assertTrue(state.canUndo)
        assertTrue(state.undo())
        val reverted = state.blockList.first() as TextBlock
        assertEquals("Hello", reverted.text)
        assertTrue(state.canRedo)

        assertTrue(state.redo())
        val redone = state.blockList.first() as TextBlock
        assertEquals("Hello World", redone.text)
    }

    @Test
    fun removeImageBeforeTextBlockDeletesImage() {
        val text = TextBlock(text = "")
        val image = ImageBlock(uri = "file://image")
        val state = NoteEditorState(NoteContent(listOf(image, text)))

        val removed = state.removeImageBefore(text.id)

        assertTrue(removed)
        assertTrue(state.blockList.none { it.id == image.id })
    }

    @Test
    fun selectedImageRemovalClearsSelection() {
        val text = TextBlock(text = "")
        val image = ImageBlock(uri = "file://image")
        val state = NoteEditorState(NoteContent(listOf(text, image)))

        state.toggleImageSelection(image.id)
        assertTrue(state.isImageSelected(image.id))

        val removed = state.removeSelectedImage()

        assertTrue(removed)
        assertTrue(state.blockList.none { it.id == image.id })
        assertTrue(!state.isImageSelected(image.id))
    }
}
