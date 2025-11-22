package com.edufelip.shared.domain.model

import kotlin.math.max
import kotlin.math.min

/** Represents the current caret or selection within the editor. */
data class Caret(
    val blockId: String,
    val start: Int,
    val end: Int = start,
) {
    init {
        require(start >= 0) { "Caret start must be non-negative" }
        require(end >= 0) { "Caret end must be non-negative" }
    }

    val normalizedStart: Int get() = min(start, end)
    val normalizedEnd: Int get() = max(start, end)
    val isRange: Boolean get() = normalizedEnd > normalizedStart
}

/** Result of inserting an image at the caret, including the resolved caret to focus next. */
data class InsertImageResult(
    val content: NoteContent,
    val nextCaret: Caret,
)

fun insertImageAtCaret(
    content: NoteContent,
    caret: Caret,
    imageUri: String,
    width: Int? = null,
    height: Int? = null,
    alt: String? = null,
): InsertImageResult = insertImageAtCaret(
    content,
    caret,
    ImageBlock(
        localUri = imageUri,
        width = width,
        height = height,
        alt = alt,
    ),
)

fun insertImageAtCaret(
    content: NoteContent,
    caret: Caret,
    imageBlock: ImageBlock,
): InsertImageResult {
    val blocks = content.blocks
    if (blocks.isEmpty()) {
        val trailing = TextBlock(text = "")
        val updated = NoteContent(listOf(imageBlock, trailing))
        return InsertImageResult(updated, Caret(trailing.id, 0))
    }
    val index = blocks.indexOfFirst { it.id == caret.blockId }
    if (index < 0) {
        val trailing = TextBlock(text = "")
        val updated = NoteContent(blocks + listOf(imageBlock, trailing))
        return InsertImageResult(updated, Caret(trailing.id, 0))
    }
    val target = blocks[index]
    val updatedBlocks = buildList {
        addAll(blocks.take(index))
        when (target) {
            is TextBlock -> {
                val start = caret.normalizedStart.coerceIn(0, target.text.length)
                val end = caret.normalizedEnd.coerceIn(start, target.text.length)
                val beforeText = target.text.substring(0, start)
                val afterText = target.text.substring(end)
                val beforeSpans = target.spans.clipRange(0, start)
                val afterSpans = target.spans.clipRange(end, target.text.length)
                if (beforeText.isNotEmpty()) {
                    add(target.copy(text = beforeText, spans = beforeSpans))
                } else if (index == 0) {
                    // Ensure there is always a leading text block to avoid the editor feeling "stuck" above the image.
                    add(TextBlock(text = "", spans = emptyList()))
                }
                add(imageBlock)
                val trailingBlock = when {
                    afterText.isNotEmpty() -> TextBlock(text = afterText, spans = afterSpans)
                    else -> TextBlock(text = "")
                }
                add(trailingBlock)
                val remaining = blocks.drop(index + 1)
                if (remaining.isNotEmpty()) addAll(remaining)
                return InsertImageResult(NoteContent(this), Caret(trailingBlock.id, 0))
            }

            is ImageBlock -> {
                add(target)
                add(imageBlock)
                val trailing = if (index + 1 < blocks.size && blocks[index + 1] is TextBlock) {
                    blocks[index + 1] as TextBlock
                } else {
                    TextBlock(text = "")
                }
                val tail = if (trailing in blocks) blocks.drop(index + 2) else blocks.drop(index + 1)
                if (trailing !in this) add(trailing)
                addAll(tail)
                return InsertImageResult(NoteContent(this), Caret(trailing.id, 0))
            }
        }
    }
    // Should never reach here, but return safe fallback
    val fallbackTrailing = TextBlock(text = "")
    return InsertImageResult(NoteContent(blocks + listOf(imageBlock, fallbackTrailing)), Caret(fallbackTrailing.id, 0))
}

private fun List<NoteTextSpan>.clipRange(start: Int, end: Int): List<NoteTextSpan> = buildList {
    for (span in this@clipRange) {
        val clippedStart = span.start.coerceIn(0, end)
        val clippedEnd = span.end.coerceIn(0, end)
        if (clippedEnd <= start) continue
        if (clippedStart >= end) continue
        if (clippedStart < start && clippedEnd <= start) continue
        val newStart = max(clippedStart, start)
        val newEnd = min(clippedEnd, end)
        if (newEnd > newStart) {
            add(
                span.copy(
                    start = newStart - start,
                    end = newEnd - start,
                ),
            )
        }
    }
}
