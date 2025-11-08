package com.edufelip.shared.ui.editor

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.edufelip.shared.domain.model.NoteBlock

/**
 * Lightweight wrapper around the editor's backing [SnapshotStateList] so callers can
 * observe block changes without copying the list on every recomposition.
 */
class EditorDocument internal constructor(
    private val blocks: SnapshotStateList<NoteBlock>,
) : List<NoteBlock> by blocks {

    internal fun asSnapshotStateList(): SnapshotStateList<NoteBlock> = blocks
}
