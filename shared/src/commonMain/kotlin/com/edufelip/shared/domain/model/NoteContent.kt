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
    legacyRemoteUri = null,
    legacyUri = storagePath ?: legacyUri,
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
