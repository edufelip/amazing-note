package com.edufelip.shared.ui.editor

import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.generateBlockId

/** Simple in-memory clipboard shared across platforms for block-level payloads. */
object EditorClipboard {
    private var imageBlocks: List<ImageBlock> = emptyList()

    fun storeImages(blocks: List<ImageBlock>) {
        imageBlocks = blocks.map { it.copy() }
    }

    fun spawnImages(): List<ImageBlock>? {
        if (imageBlocks.isEmpty()) return null
        return imageBlocks.map { block -> block.copy(id = generateBlockId()) }
    }

    fun hasImages(): Boolean = imageBlocks.isNotEmpty()
}
