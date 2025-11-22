package com.edufelip.shared.ui.editor

/** Represents a cursor anchored to a specific block and offset within that block. */
data class BlockCursor(
    val blockId: String,
    val offset: Int,
) {
    init {
        require(offset >= 0) { "Cursor offset must be non-negative" }
    }
}

/**
 * Multi-block selection primitive. Anchor is where the drag started, focus is the
 * actively moving edge (matching Compose text selection semantics).
 */
data class DocSelection(
    val anchor: BlockCursor,
    val focus: BlockCursor,
) {
    val isCollapsed: Boolean get() = anchor == focus

    fun normalized(orderResolver: (String) -> Int): Pair<BlockCursor, BlockCursor> {
        val anchorIndex = orderResolver(anchor.blockId)
        val focusIndex = orderResolver(focus.blockId)
        return if (anchorIndex < focusIndex || (anchorIndex == focusIndex && anchor.offset <= focus.offset)) {
            anchor to focus
        } else {
            focus to anchor
        }
    }

    companion object {
        fun collapsed(blockId: String, offset: Int): DocSelection {
            val cursor = BlockCursor(blockId, offset)
            return DocSelection(cursor, cursor)
        }
    }
}
