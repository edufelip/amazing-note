package com.edufelip.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.random.Random

private const val BLOCK_ID_LENGTH = 12
private const val LEGACY_SPANS_KEY = "legacy_spans_json"

private val noteContentJson = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "type"
    encodeDefaults = false
    explicitNulls = false
}

private val legacyBlocksJson = Json {
    ignoreUnknownKeys = true
}

@Serializable
sealed interface NoteBlock {
    val id: String
}

@Serializable
@SerialName("text")
data class TextBlock(
    override val id: String = generateBlockId(),
    val text: String,
    val spans: List<NoteTextSpan> = emptyList(),
) : NoteBlock

@Serializable
@SerialName("image")
data class ImageBlock(
    override val id: String = generateBlockId(),
    val uri: String,
    val width: Int? = null,
    val height: Int? = null,
    val alt: String? = null,
    val thumbnailUri: String? = null,
    val mimeType: String? = null,
    val fileName: String? = null,
    val remoteUri: String? = null,
) : NoteBlock

@Serializable
data class NoteContent(
    val blocks: List<NoteBlock> = emptyList(),
) {
    companion object
}

fun NoteContent.Companion.empty(): NoteContent = NoteContent()

fun NoteContent.toJson(): String = noteContentJson.encodeToString(NoteContent.serializer(), this)

fun noteContentFromJson(raw: String?): NoteContent = raw
    ?.takeIf { it.isNotBlank() }
    ?.let { json ->
        runCatching { noteContentJson.decodeFromString(NoteContent.serializer(), json) }.getOrNull()
    }
    ?: NoteContent()

private val blockIdAlphabet = ('a'..'z') + ('0'..'9')

fun generateBlockId(): String = buildString(capacity = BLOCK_ID_LENGTH) {
    repeat(BLOCK_ID_LENGTH) {
        append(blockIdAlphabet[Random.nextInt(blockIdAlphabet.size)])
    }
}

fun NoteAttachment.toImageBlock(): ImageBlock = ImageBlock(
    id = if (id.isBlank()) generateBlockId() else id,
    uri = downloadUrl,
    width = width,
    height = height,
    alt = fileName,
    thumbnailUri = thumbnailUrl,
    mimeType = mimeType,
    fileName = fileName,
    remoteUri = downloadUrl.takeIf { it.startsWith("http", ignoreCase = true) || it.startsWith("https", ignoreCase = true) },
)

fun ImageBlock.toAttachment(): NoteAttachment = NoteAttachment(
    id = id,
    downloadUrl = remoteUri ?: uri,
    thumbnailUrl = thumbnailUri,
    mimeType = mimeType ?: "image/*",
    fileName = fileName ?: alt,
    width = width,
    height = height,
)

data class LegacyContent(
    val description: String,
    val spans: List<NoteTextSpan>,
    val attachments: List<NoteAttachment>,
)

fun NoteContent.toLegacyContent(): LegacyContent {
    if (blocks.isEmpty()) return LegacyContent("", emptyList(), emptyList())
    val textBlocks = blocks.filterIsInstance<TextBlock>()
    val description = textBlocks.joinToString(separator = "\n\n") { it.text }
    val spans = textBlocks.firstOrNull()?.spans ?: emptyList()
    val attachments = blocks.mapNotNull { block -> (block as? ImageBlock)?.toAttachment() }
    return LegacyContent(description, spans, attachments)
}

fun noteContentFromLegacy(
    description: String,
    spans: List<NoteTextSpan>,
    attachments: List<NoteAttachment>,
): NoteContent {
    val blocks = buildList {
        if (description.isNotBlank()) {
            add(TextBlock(text = description, spans = spans))
        }
        attachments.forEach { attachment ->
            add(attachment.toImageBlock())
        }
    }
    return NoteContent(blocks)
}

fun noteContentFromLegacyBlocksJson(raw: String?): NoteContent {
    val legacyBlocks = raw
        ?.takeIf { it.isNotBlank() }
        ?.let { json ->
            runCatching { legacyBlocksJson.decodeFromString(ListSerializer(LegacyNoteBlock.serializer()), json) }.getOrNull()
        }
        ?: emptyList()
    if (legacyBlocks.isEmpty()) return NoteContent()
    val blocks = legacyBlocks
        .sortedBy { it.order }
        .mapNotNull { block ->
            when (block.type.uppercase()) {
                "TEXT", "HEADING_1", "HEADING_2", "HEADING_3", "BULLET_LIST", "NUMBERED_LIST", "CODE", "QUOTE" ->
                    TextBlock(id = block.id, text = block.content, spans = metadataToSpans(block.metadata))
                "IMAGE" -> metadataToAttachment(block.metadata)?.toImageBlock()?.copy(
                    id = block.id,
                    alt = block.metadata["caption"] ?: block.metadata["fileName"] ?: block.metadata["attachmentId"],
                )
                else -> null
            }
        }
    return NoteContent(blocks)
}

fun NoteContent.ensure(description: String, spans: List<NoteTextSpan>, attachments: List<NoteAttachment>): NoteContent = if (blocks.isNotEmpty()) this else noteContentFromLegacy(description, spans, attachments)

fun ensureContent(
    description: String,
    spans: List<NoteTextSpan>,
    attachments: List<NoteAttachment>,
    content: NoteContent,
): NoteContent = content.ensure(description, spans, attachments)

fun Note.ensureContent(): Note {
    if (content.blocks.isNotEmpty()) return this
    val derived = noteContentFromLegacy(description, descriptionSpans, attachments)
    return copy(content = derived)
}

fun Note.withLegacyFieldsFromContent(): Note {
    if (content.blocks.isEmpty()) return this
    val legacy = content.toLegacyContent()
    val mergedDescription = if (legacy.description.isNotBlank()) legacy.description else description
    val mergedSpans = if (legacy.spans.isNotEmpty()) legacy.spans else descriptionSpans
    val mergedAttachments = if (legacy.attachments.isNotEmpty()) legacy.attachments else attachments
    return copy(
        description = mergedDescription,
        descriptionSpans = mergedSpans,
        attachments = mergedAttachments,
    )
}

fun NoteBlock.asAttachment(): NoteAttachment? = (this as? ImageBlock)?.toAttachment()

@Serializable
private data class LegacyNoteBlock(
    val id: String,
    val type: String,
    val content: String = "",
    val metadata: Map<String, String> = emptyMap(),
    val order: Int = 0,
)

private fun metadataToAttachment(metadata: Map<String, String>): NoteAttachment? {
    val url = metadata["url"] ?: metadata["downloadUrl"] ?: return null
    return NoteAttachment(
        id = metadata["attachmentId"] ?: metadata["id"] ?: url,
        downloadUrl = url,
        thumbnailUrl = metadata["thumbnailUrl"],
        mimeType = metadata["mimeType"] ?: "image/*",
        fileName = metadata["fileName"],
        width = metadata["width"]?.toIntOrNull(),
        height = metadata["height"]?.toIntOrNull(),
    )
}

private fun metadataToSpans(metadata: Map<String, String>): List<NoteTextSpan> = metadata[LEGACY_SPANS_KEY]
    ?.let { spansFromJson(it) }
    ?: emptyList()
