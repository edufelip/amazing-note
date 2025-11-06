package com.edufelip.shared.data.cloud

import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.attachmentsFromJson
import com.edufelip.shared.domain.model.ensureContent
import com.edufelip.shared.domain.model.noteContentFromJson
import com.edufelip.shared.domain.model.noteContentFromLegacyBlocksJson
import com.edufelip.shared.domain.model.spansFromJson
import com.edufelip.shared.domain.model.toJson
import com.edufelip.shared.domain.model.withLegacyFieldsFromContent
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.firestoreSettings
import dev.gitlive.firebase.firestore.memoryCacheSettings
import dev.gitlive.firebase.firestore.memoryEagerGcSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface CloudNotesDataSource {
    fun observe(uid: String): Flow<List<Note>>
    suspend fun getAll(uid: String): List<Note>
    suspend fun upsert(uid: String, note: Note)
    suspend fun delete(uid: String, id: Int)

    // Upsert that preserves provided updatedAt (no server timestamp). Useful for push-only sync to avoid reordering.
    suspend fun upsertPreserveUpdatedAt(uid: String, note: Note)
}

fun provideCloudNotesDataSource(): CloudNotesDataSource = GitLiveCloudNotesDataSource

interface CurrentUserProvider {
    val uid: Flow<String?>
}

fun provideCurrentUserProvider(): CurrentUserProvider = GitLiveCurrentUserProvider

private object GitLiveCloudNotesDataSource : CloudNotesDataSource {
    init {
        Firebase.firestore.settings = firestoreSettings {
            cacheSettings = memoryCacheSettings {
                gcSettings = memoryEagerGcSettings { }
            }
        }
    }

    override fun observe(uid: String): Flow<List<Note>> = notesCollection(uid)
        .snapshots
        .map { snapshot ->
            snapshot.documents
                .mapNotNull { document -> document.toNoteOrNull() }
                .sortedBy { it.updatedAt }
        }

    override suspend fun getAll(uid: String): List<Note> = notesCollection(uid)
        .get()
        .documents
        .mapNotNull { it.toNoteOrNull() }
        .sortedBy { it.updatedAt }

    override suspend fun upsert(uid: String, note: Note) {
        val data = note.toFirestoreData(useServerUpdatedAt = true)
        notesCollection(uid)
            .document(note.id.toString())
            .set(data, merge = true)
    }

    override suspend fun delete(uid: String, id: Int) {
        notesCollection(uid)
            .document(id.toString())
            .delete()
    }

    override suspend fun upsertPreserveUpdatedAt(uid: String, note: Note) {
        val data = note.toFirestoreData(useServerUpdatedAt = false)
        notesCollection(uid)
            .document(note.id.toString())
            .set(data, merge = true)
    }
}

private fun notesCollection(uid: String) = Firebase.firestore
    .collection("users")
    .document(uid)
    .collection("notes")

private fun Note.toFirestoreData(useServerUpdatedAt: Boolean): Map<String, Any?> = buildMap {
    put("id", id)
    put("title", title)
    put("description", description)
    put("descriptionSpans", descriptionSpans.toJson())
    put("attachments", attachments.toJson())
    put("contentJson", content.toJson())
    put("blocks", "[]")
    put("deleted", deleted)
    put("folderId", folderId)
    put(
        "createdAt",
        if (createdAt == 0L) FieldValue.serverTimestamp else createdAt,
    )
    put(
        "updatedAt",
        if (useServerUpdatedAt) FieldValue.serverTimestamp else updatedAt,
    )
}

private fun DocumentSnapshot.toNoteOrNull(): Note? {
    val data = runCatching { data<Map<String, Any?>>() }.getOrNull() ?: return null
    return data.toNote(id)
}

private fun Map<String, Any?>.toNote(docId: String): Note? {
    val rawTitle = this["title"] as? String ?: return null
    val description = this["description"] as? String ?: ""
    val spans = spansFromJson(this["descriptionSpans"] as? String)
    val attachments = attachmentsFromJson(this["attachments"] as? String)
    val parsedContent = noteContentFromJson(this["contentJson"] as? String ?: this["content_json"] as? String)
    val legacyContent = if (parsedContent.blocks.isNotEmpty()) parsedContent else noteContentFromLegacyBlocksJson(this["blocks"] as? String)
    val ensuredContent = ensureContent(description, spans, attachments, legacyContent)
    val note = Note(
        id = (this["id"] as? Number)?.toInt() ?: docId.toIntOrNull() ?: -1,
        title = rawTitle,
        description = description,
        descriptionSpans = spans,
        attachments = attachments,
        content = ensuredContent,
        deleted = (this["deleted"] as? Boolean) ?: false,
        createdAt = this["createdAt"].toMillis() ?: 0L,
        updatedAt = this["updatedAt"].toMillis() ?: 0L,
        folderId = (this["folderId"] as? Number)?.toLong(),
    )
    return note.ensureContent().withLegacyFieldsFromContent()
}

private fun Any?.toMillis(): Long? = when (this) {
    is Number -> toLong()
    is Timestamp -> (seconds * 1_000L) + (nanoseconds / 1_000_000L)
    else -> null
}

private object GitLiveCurrentUserProvider : CurrentUserProvider {
    override val uid: Flow<String?> =
        Firebase.auth.authStateChanged.map { user -> user?.uid }
}
