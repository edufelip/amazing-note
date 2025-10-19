package com.edufelip.shared.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val blockJsonFormatter = Json {
    ignoreUnknownKeys = true
}

/**
 * Represents a granular piece of note content. Notes are composed of ordered blocks
 * so that rich elements (headings, lists, images, etc.) can be arranged flexibly.
 */
@Serializable
data class NoteBlock(
    val id: String,
    val type: BlockType,
    val content: String = "",
    val metadata: Map<String, String> = emptyMap(),
    val order: Int = 0,
)

/**
 * Enumerates the supported block types. These mirror the editor surface components.
 */
@Serializable
enum class BlockType {
    TEXT,
    HEADING_1,
    HEADING_2,
    HEADING_3,
    IMAGE,
    BULLET_LIST,
    NUMBERED_LIST,
    CODE,
    QUOTE,
}

internal fun List<NoteBlock>.blocksToJson(): String = blockJsonFormatter.encodeToString(this)

internal fun blocksFromJson(raw: String?): List<NoteBlock> = raw
    ?.takeIf { it.isNotBlank() }
    ?.let { json ->
        runCatching { blockJsonFormatter.decodeFromString<List<NoteBlock>>(json) }.getOrNull()
    }
    ?: emptyList()

internal const val LEGACY_SPANS_KEY = "legacy_spans_json"

internal fun legacyBlockId(type: BlockType, order: Int): String = "legacy-${type.name.lowercase()}-$order"

internal fun NoteAttachment.toBlockMetadata(): Map<String, String> = buildMap {
    put("url", downloadUrl)
    thumbnailUrl?.let { put("thumbnailUrl", it) }
    put("mimeType", mimeType)
    fileName?.let { put("fileName", it) }
    width?.let { put("width", it.toString()) }
    height?.let { put("height", it.toString()) }
    put("attachmentId", id)
}

fun NoteAttachment.toImageBlock(order: Int): NoteBlock = NoteBlock(
    id = id.ifBlank { legacyBlockId(BlockType.IMAGE, order) },
    type = BlockType.IMAGE,
    content = "",
    metadata = toBlockMetadata(),
    order = order,
)

internal fun metadataToAttachment(metadata: Map<String, String>): NoteAttachment? {
    val url = metadata["url"] ?: return null
    return NoteAttachment(
        id = metadata["attachmentId"] ?: url,
        downloadUrl = url,
        thumbnailUrl = metadata["thumbnailUrl"],
        mimeType = metadata["mimeType"] ?: "image/*",
        fileName = metadata["fileName"],
        width = metadata["width"]?.toIntOrNull(),
        height = metadata["height"]?.toIntOrNull(),
    )
}

private fun metadataToSpans(metadata: Map<String, String>): List<NoteTextSpan> = metadata[LEGACY_SPANS_KEY]?.let { spansFromJson(it) } ?: emptyList()

data class LegacyContent(
    val description: String,
    val spans: List<NoteTextSpan>,
    val attachments: List<NoteAttachment>,
)

fun blocksToLegacyContent(blocks: List<NoteBlock>): LegacyContent {
    if (blocks.isEmpty()) return LegacyContent("", emptyList(), emptyList())
    val textBlocks = blocks.filter { it.type == BlockType.TEXT }
    val description = textBlocks.joinToString(separator = "\n\n") { it.content }
    val spans = textBlocks.firstOrNull()?.let { metadataToSpans(it.metadata) } ?: emptyList()
    val attachments = blocks.filter { it.type == BlockType.IMAGE }
        .mapNotNull { metadataToAttachment(it.metadata) }
    return LegacyContent(description, spans, attachments)
}

fun legacyBlocksFrom(
    description: String,
    spans: List<NoteTextSpan>,
    attachments: List<NoteAttachment>,
): List<NoteBlock> {
    var order = 0
    val blocks = mutableListOf<NoteBlock>()
    if (description.isNotBlank()) {
        val metadata = if (spans.isNotEmpty()) {
            mapOf(LEGACY_SPANS_KEY to spans.toJson())
        } else {
            emptyMap()
        }
        blocks += NoteBlock(
            id = legacyBlockId(BlockType.TEXT, order),
            type = BlockType.TEXT,
            content = description,
            metadata = metadata,
            order = order,
        )
        order += 1
    }
    attachments.forEach { attachment ->
        blocks += attachment.toImageBlock(order)
        order += 1
    }
    return blocks
}

fun ensureBlocks(
    description: String,
    spans: List<NoteTextSpan>,
    attachments: List<NoteAttachment>,
    blocks: List<NoteBlock>,
): List<NoteBlock> = if (blocks.isNotEmpty()) blocks else legacyBlocksFrom(description, spans, attachments)

fun Note.ensureBlocks(): Note = if (blocks.isNotEmpty()) this else copy(blocks = legacyBlocksFrom(description, descriptionSpans, attachments))

fun Note.withLegacyFieldsFromBlocks(): Note {
    if (blocks.isEmpty()) return this
    val legacy = blocksToLegacyContent(blocks)
    return copy(
        description = legacy.description,
        descriptionSpans = legacy.spans,
        attachments = if (legacy.attachments.isNotEmpty()) legacy.attachments else attachments,
    )
}

fun NoteBlock.asAttachment(): NoteAttachment? = if (type == BlockType.IMAGE) metadataToAttachment(metadata) else null
