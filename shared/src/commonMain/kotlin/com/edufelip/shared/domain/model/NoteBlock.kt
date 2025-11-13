package com.edufelip.shared.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.random.Random

private const val BLOCK_ID_LENGTH = 12
private val BLOCK_ID_ALPHABET = ('a'..'z') + ('0'..'9')

@Serializable
enum class ImageSyncState {
    PendingUpload,
    Synced,
    Error,
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
    val localUri: String? = null,
    val storagePath: String? = null,
    val syncState: ImageSyncState = ImageSyncState.Synced,
) : NoteBlock

fun generateBlockId(): String = buildString(capacity = BLOCK_ID_LENGTH) {
    repeat(BLOCK_ID_LENGTH) {
        append(BLOCK_ID_ALPHABET[Random.nextInt(BLOCK_ID_ALPHABET.size)])
    }
}
