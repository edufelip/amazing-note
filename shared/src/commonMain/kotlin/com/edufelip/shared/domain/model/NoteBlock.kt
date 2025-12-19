package com.edufelip.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

private const val BLOCK_ID_LENGTH = 12
private val BLOCK_ID_ALPHABET = ('a'..'z') + ('0'..'9')

@Serializable
enum class ImageSyncState {
    PendingUpload,
    Uploading,
    Synced,
    UploadFailed,
}

@Serializable
data class ImageMetadata(
    val width: Int? = null,
    val height: Int? = null,
    val fileSizeBytes: Long? = null,
    val mimeType: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
)

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
) : NoteBlock {
    override fun equals(other: Any?): Boolean = other is TextBlock &&
        other.text == text &&
        other.spans == spans

    override fun hashCode(): Int = super.hashCode()
}

@Serializable
@SerialName("image")
data class ImageBlock(
    override val id: String = generateBlockId(),
    val localUri: String? = null,
    val storagePath: String? = null,
    val thumbnailLocalUri: String? = null,
    val thumbnailStoragePath: String? = null,
    val syncState: ImageSyncState = ImageSyncState.PendingUpload,
    val metadata: ImageMetadata = ImageMetadata(),
    val alt: String? = null,
    val fileName: String? = null,
    /** Legacy remote URL stored on older records. */
    val legacyRemoteUri: String? = null,
    /** Resolved download URL derived from storagePath when fetched from cloud. */
    val resolvedDownloadUrl: String? = null,
    /** Resolved download URL for thumbnail. */
    val resolvedThumbnailUrl: String? = null,
    /** Local cached copy of the resolved download URL. */
    val cachedRemoteUri: String? = null,
    /** Local cached copy of the resolved thumbnail URL. */
    val cachedThumbnailUri: String? = null,
    val mimeType: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val thumbnailUri: String? = null,
) : NoteBlock {
    override fun equals(other: Any?): Boolean = other is ImageBlock &&
        other.resolvedDownloadUrl == resolvedDownloadUrl

    override fun hashCode(): Int = super.hashCode()
}

fun generateBlockId(): String = buildString(capacity = BLOCK_ID_LENGTH) {
    repeat(BLOCK_ID_LENGTH) {
        append(BLOCK_ID_ALPHABET[Random.nextInt(BLOCK_ID_ALPHABET.size)])
    }
}
