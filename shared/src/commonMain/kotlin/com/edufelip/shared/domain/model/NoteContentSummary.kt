package com.edufelip.shared.domain.model

data class NoteContentSummary(
    val description: String,
    val spans: List<NoteTextSpan>,
    val attachments: List<NoteAttachment>,
)

fun NoteContent.toSummary(): NoteContentSummary {
    if (blocks.isEmpty()) return NoteContentSummary("", emptyList(), emptyList())
    val textBlocks = blocks.filterIsInstance<TextBlock>()
    val description = textBlocks.joinToString(separator = "\n\n") { it.text }
    val spans = textBlocks.firstOrNull()?.spans ?: emptyList()
    val attachments = blocks.filterIsInstance<ImageBlock>().map { it.toAttachment() }
    return NoteContentSummary(description, spans, attachments)
}

fun NoteContentSummary.withFallbacks(
    fallbackDescription: String,
    fallbackSpans: List<NoteTextSpan>,
    fallbackAttachments: List<NoteAttachment>,
): NoteContentSummary = NoteContentSummary(
    description = description.ifBlank { fallbackDescription },
    spans = spans.ifEmpty { fallbackSpans },
    attachments = attachments.ifEmpty { fallbackAttachments },
)

fun Note.withSummary(summary: NoteContentSummary): Note = copy(
    description = summary.description,
    descriptionSpans = summary.spans,
    attachments = summary.attachments,
)

fun Note.withSummaryFromContent(): Note = withSummary(
    content.toSummary().withFallbacks(description, descriptionSpans, attachments),
)

fun ImageBlock.toAttachment(): NoteAttachment = NoteAttachment(
    id = id,
    downloadUrl = storagePath ?: legacyRemoteUri ?: legacyUri ?: localUri.orEmpty(),
    thumbnailUrl = thumbnailStoragePath ?: thumbnailUri,
    mimeType = metadata.mimeType ?: mimeType ?: "image/*",
    fileName = fileName ?: alt,
    width = metadata.width ?: width,
    height = metadata.height ?: height,
    storagePath = storagePath,
    thumbnailStoragePath = thumbnailStoragePath,
    localUri = localUri,
    syncState = syncState,
    fileSizeBytes = metadata.fileSizeBytes,
    createdAt = metadata.createdAt,
    updatedAt = metadata.updatedAt,
)
