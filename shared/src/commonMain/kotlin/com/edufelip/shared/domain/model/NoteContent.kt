package com.edufelip.shared.domain.model

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.jvm.JvmName

private val noteContentJson = Json {
    ignoreUnknownKeys = true
    classDiscriminator = "type"
    encodeDefaults = false
    explicitNulls = false
}

private val jsonFormatter = Json { ignoreUnknownKeys = true }

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
    ?.normalizedForSync()
    ?: NoteContent()

@Serializable
data class NoteAttachment(
    val id: String,
    val downloadUrl: String,
    val thumbnailUrl: String? = null,
    val mimeType: String = "image/*",
    val fileName: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val storagePath: String? = null,
    val thumbnailStoragePath: String? = null,
    val localUri: String? = null,
    val syncState: ImageSyncState? = null,
    val fileSizeBytes: Long? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
)

@Serializable
enum class NoteTextStyle {
    Bold,
    Italic,
    Underline,
}

@Serializable
data class NoteTextSpan(
    val start: Int,
    val end: Int,
    val style: NoteTextStyle,
) {
    fun toSpanStyle(): SpanStyle = when (style) {
        NoteTextStyle.Bold -> SpanStyle(fontWeight = FontWeight.SemiBold)
        NoteTextStyle.Italic -> SpanStyle(fontStyle = FontStyle.Italic)
        NoteTextStyle.Underline -> SpanStyle(textDecoration = TextDecoration.Underline)
    }
}

data class NoteRichText(
    val text: String,
    val spans: List<NoteTextSpan>,
) {
    fun toAnnotatedString(): AnnotatedString = buildAnnotatedString {
        append(text)
        spans.forEach { span ->
            val rangeEnd = span.end.coerceAtMost(text.length)
            val rangeStart = span.start.coerceIn(0, rangeEnd)
            if (rangeStart < rangeEnd) {
                addStyle(span.toSpanStyle(), rangeStart, rangeEnd)
            }
        }
    }
}

@JvmName("attachmentsToJson")
fun List<NoteAttachment>.toJson(): String = jsonFormatter.encodeToString(this)

fun attachmentsFromJson(raw: String?): List<NoteAttachment> = raw
    ?.takeIf { it.isNotBlank() }
    ?.let {
        runCatching { jsonFormatter.decodeFromString<List<NoteAttachment>>(it) }.getOrNull()
    }
    ?: emptyList()

@JvmName("spansToJson")
fun List<NoteTextSpan>.toJson(): String = jsonFormatter.encodeToString(this)

fun spansFromJson(raw: String?): List<NoteTextSpan> = raw
    ?.takeIf { it.isNotBlank() }
    ?.let {
        runCatching { jsonFormatter.decodeFromString<List<NoteTextSpan>>(it) }.getOrNull()
    }
    ?: emptyList()

fun NoteContent.remoteSafe(): NoteContent = copy(
    blocks = blocks.map { block ->
        when (block) {
            is ImageBlock -> block.remoteSafe()
            else -> block
        }
    },
)

fun ImageBlock.remoteSafe(): ImageBlock = copy(
    localUri = null,
    thumbnailLocalUri = null,
    thumbnailUri = null,
    syncState = if (storagePath != null) ImageSyncState.Synced else syncState,
)

fun List<NoteAttachment>.remoteSafe(): List<NoteAttachment> = map { attachment ->
    val remoteUrl = attachment.storagePath ?: ""
    attachment.copy(
        downloadUrl = remoteUrl,
        thumbnailUrl = attachment.thumbnailStoragePath ?: attachment.thumbnailUrl,
        localUri = null,
    )
}

fun NoteContent.normalizedForSync(): NoteContent = copy(
    blocks = blocks.map { block ->
        when (block) {
            is ImageBlock -> block.normalizedForSync()
            else -> block
        }
    },
)

/** Capture storage and cached URIs for diffing and cleanup. */
fun NoteContent.imagePaths(): List<String> = blocks
    .filterIsInstance<ImageBlock>()
    .flatMap { image ->
        listOfNotNull(
            image.storagePath,
            image.thumbnailStoragePath,
            image.cachedRemoteUri,
            image.cachedThumbnailUri,
        )
    }

private fun ImageBlock.normalizedForSync(): ImageBlock {
    val mergedMetadata = metadata.copy(
        width = metadata.width ?: width,
        height = metadata.height ?: height,
        mimeType = metadata.mimeType ?: mimeType,
    )
    val shouldMarkSynced = storagePath != null && syncState != ImageSyncState.Synced
    return copy(
        metadata = mergedMetadata,
        syncState = if (shouldMarkSynced) ImageSyncState.Synced else syncState,
    )
}

/** Promote cached URIs into local fields and retain resolved remote URLs. */
fun NoteContent.normalizeCachedImages(): NoteContent = copy(
    blocks = blocks.map { block ->
        when (block) {
            is ImageBlock -> block.copy(
                localUri = block.localUri ?: block.cachedRemoteUri,
                thumbnailLocalUri = block.thumbnailLocalUri ?: block.cachedThumbnailUri,
                resolvedDownloadUrl = block.resolvedDownloadUrl ?: block.legacyRemoteUri,
                resolvedThumbnailUrl = block.resolvedThumbnailUrl ?: block.thumbnailUri,
                legacyRemoteUri = block.legacyRemoteUri ?: block.resolvedDownloadUrl,
            )
            else -> block
        }
    },
)

/** Merge cached local URIs from [other] into this content based on storage or block id. */
fun NoteContent.mergeCachedImages(other: NoteContent): NoteContent {
    val cacheByKey = other.blocks.filterIsInstance<ImageBlock>().associateBy { keyForImage(it) }
    return copy(
        blocks = blocks.map { block ->
            if (block !is ImageBlock) return@map block
            val key = keyForImage(block)
            val cached = cacheByKey[key] ?: return@map block
            block.copy(
                cachedRemoteUri = block.cachedRemoteUri ?: cached.cachedRemoteUri,
                cachedThumbnailUri = block.cachedThumbnailUri ?: cached.cachedThumbnailUri,
                localUri = block.localUri ?: cached.localUri ?: cached.cachedRemoteUri,
                thumbnailLocalUri = block.thumbnailLocalUri ?: cached.thumbnailLocalUri ?: cached.cachedThumbnailUri,
                legacyRemoteUri = block.legacyRemoteUri ?: cached.legacyRemoteUri,
                resolvedDownloadUrl = block.resolvedDownloadUrl ?: cached.resolvedDownloadUrl ?: cached.legacyRemoteUri,
                resolvedThumbnailUrl = block.resolvedThumbnailUrl ?: cached.resolvedThumbnailUrl,
            )
        },
    )
}

private fun keyForImage(image: ImageBlock): String =
    image.storagePath
        ?: image.thumbnailStoragePath
        ?: image.id

fun NoteContent.trimEmptyTextBlocks(): NoteContent = copy(
    blocks = blocks.filterNot { block ->
        block is TextBlock && block.text.isBlank() && block.spans.isEmpty()
    },
)
