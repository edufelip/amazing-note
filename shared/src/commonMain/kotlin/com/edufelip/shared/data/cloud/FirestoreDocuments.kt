package com.edufelip.shared.data.cloud

import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.attachmentsFromJson
import com.edufelip.shared.domain.model.noteContentFromJson
import com.edufelip.shared.domain.model.spansFromJson
import com.edufelip.shared.domain.model.toSummary
import com.edufelip.shared.domain.model.withFallbacks
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.Serializable

internal fun DocumentSnapshot.toNoteOrNull(): Note? = runCatching {
    data(NoteDoc.serializer()).toDomainNote(id)
}.onFailure { throwable ->
    logDecodeError("note", id, throwable)
}.getOrNull()

internal fun DocumentSnapshot.toFolderOrNull(): Folder? = runCatching {
    data(FolderDoc.serializer()).toDomainFolder(id)
}.onFailure { throwable ->
    logDecodeError("folder", id, throwable)
}.getOrNull()

internal fun Timestamp?.toMillis(): Long? = this?.let { it.seconds * 1_000L + it.nanoseconds / 1_000_000L }

internal fun NoteDoc.toDomainNote(fallbackId: String): Note {
    val content = noteContentFromJson(contentJson)
    val spans = spansFromJson(descriptionSpans)
    val attachments = attachmentsFromJson(attachments)
    val summary = content.toSummary().withFallbacks(description, spans, attachments)
    val resolvedId = id ?: fallbackId.toIntOrNull() ?: -1
    val resolvedStableId = stableId?.takeIf { it.isNotBlank() } ?: fallbackId
    return Note(
        id = resolvedId,
        stableId = resolvedStableId,
        title = title,
        description = summary.description,
        descriptionSpans = summary.spans,
        attachments = summary.attachments,
        content = content,
        deleted = deleted ?: false,
        createdAt = createdAt.toMillis() ?: 0L,
        updatedAt = updatedAt.toMillis() ?: 0L,
        folderId = folderId,
    )
}

internal fun FolderDoc.toDomainFolder(fallbackId: String): Folder? {
    val resolvedId = id ?: fallbackId.toLongOrNull() ?: return null
    return Folder(
        id = resolvedId,
        name = name,
        createdAt = createdAt.toMillis() ?: 0L,
        updatedAt = updatedAt.toMillis() ?: 0L,
    )
}

internal fun Any?.toMillis(): Long? = when (this) {
    is Number -> toLong()
    is Timestamp -> (seconds * 1_000L) + (nanoseconds / 1_000_000L)
    else -> null
}

internal fun logDecodeError(entity: String, documentId: String, throwable: Throwable) {
    println("CloudNotesDataSource: Failed to decode $entity document $documentId -> ${throwable.message}")
}

@Serializable
internal data class NoteDoc(
    val id: Int? = null,
    val stableId: String? = null,
    val title: String = "",
    val description: String = "",
    val descriptionSpans: String? = null,
    val attachments: String? = null,
    val contentJson: String? = null,
    val deleted: Boolean? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val folderId: Long? = null,
)

@Serializable
internal data class FolderDoc(
    val id: Long? = null,
    val name: String = "",
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
)

@Serializable
internal data class UserDoc(
    val notes: Map<String, NoteDoc> = emptyMap(),
    val folders: Map<String, FolderDoc> = emptyMap(),
)
