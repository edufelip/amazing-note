package com.edufelip.shared.ui.editor

import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.TextBlock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
}
