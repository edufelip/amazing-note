package com.edufelip.shared.domain.model

import kotlin.random.Random

data class Note(
    val id: Int,
    val title: String,
    val description: String,
    val deleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val dirty: Boolean = false,
    val localUpdatedAt: Long = 0L,
    val folderId: Long? = null,
    val descriptionSpans: List<NoteTextSpan> = emptyList(),
    val attachments: List<NoteAttachment> = emptyList(),
    val content: NoteContent = NoteContent(),
    val stableId: String = generateStableNoteId(),
)

private const val NOTE_STABLE_ID_LENGTH = 20
private val NOTE_ID_ALPHABET = ('a'..'z') + ('0'..'9')

fun generateStableNoteId(): String = buildString(capacity = NOTE_STABLE_ID_LENGTH) {
    repeat(NOTE_STABLE_ID_LENGTH) {
        append(NOTE_ID_ALPHABET[Random.nextInt(NOTE_ID_ALPHABET.size)])
    }
}
