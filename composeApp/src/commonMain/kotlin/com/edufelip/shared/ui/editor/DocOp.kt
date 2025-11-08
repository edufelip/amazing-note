package com.edufelip.shared.ui.editor

import androidx.compose.ui.text.TextRange
import com.edufelip.shared.domain.model.Caret
import com.edufelip.shared.domain.model.NoteContent

internal sealed interface DocOp {
    fun undo(state: NoteEditorState)
    fun redo(state: NoteEditorState)
    fun mergeWith(next: DocOp): DocOp? = null
}

internal data class TextChangeOp(
    val blockId: String,
    val beforeText: String,
    val afterText: String,
    val beforeSelection: TextRange,
    val afterSelection: TextRange,
) : DocOp {
    override fun undo(state: NoteEditorState) {
        state.applyTextChangeFromHistory(blockId, beforeText, beforeSelection)
    }

    override fun redo(state: NoteEditorState) {
        state.applyTextChangeFromHistory(blockId, afterText, afterSelection)
    }

    override fun mergeWith(next: DocOp): DocOp? {
        if (next !is TextChangeOp) return null
        if (next.blockId != blockId) return null
        return TextChangeOp(
            blockId = blockId,
            beforeText = beforeText,
            afterText = next.afterText,
            beforeSelection = beforeSelection,
            afterSelection = next.afterSelection,
        )
    }
}

internal data class ContentReplaceOp(
    val beforeContent: NoteContent,
    val afterContent: NoteContent,
    val beforeCaret: Caret?,
    val afterCaret: Caret?,
) : DocOp {
    override fun undo(state: NoteEditorState) {
        state.replaceContentFromHistory(beforeContent, beforeCaret)
    }

    override fun redo(state: NoteEditorState) {
        state.replaceContentFromHistory(afterContent, afterCaret)
    }
}
