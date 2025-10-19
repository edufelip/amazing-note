package com.edufelip.shared.cloud

import com.edufelip.shared.model.Note
import com.edufelip.shared.model.attachmentsFromJson
import com.edufelip.shared.model.blocksFromJson
import com.edufelip.shared.model.blocksToJson
import com.edufelip.shared.model.ensureBlocks
import com.edufelip.shared.model.spansFromJson
import com.edufelip.shared.model.toJson
import com.edufelip.shared.model.withLegacyFieldsFromBlocks
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
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
    override fun observe(uid: String): Flow<List<Note>> =
        notesCollection(uid)
            .snapshots
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { document -> document.toNoteOrNull() }
                    .sortedBy { it.updatedAt }
            }

    override suspend fun getAll(uid: String): List<Note> =
        notesCollection(uid)
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

private fun notesCollection(uid: String) =
    Firebase.firestore
        .collection("users")
        .document(uid)
        .collection("notes")

private fun Note.toFirestoreData(useServerUpdatedAt: Boolean): Map<String, Any?> = buildMap {
    put("id", id)
    put("title", title)
    put("description", description)
    put("descriptionSpans", descriptionSpans.toJson())
    put("attachments", attachments.toJson())
    put("blocks", blocks.blocksToJson())
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
    val note = Note(
        id = (this["id"] as? Number)?.toInt() ?: docId.toIntOrNull() ?: -1,
        title = rawTitle,
        description = this["description"] as? String ?: "",
        descriptionSpans = spansFromJson(this["descriptionSpans"] as? String),
        attachments = attachmentsFromJson(this["attachments"] as? String),
        blocks = blocksFromJson(this["blocks"] as? String),
        deleted = (this["deleted"] as? Boolean) ?: false,
        createdAt = this["createdAt"].toMillis() ?: 0L,
        updatedAt = this["updatedAt"].toMillis() ?: 0L,
        folderId = (this["folderId"] as? Number)?.toLong(),
    )
    return note.ensureBlocks().withLegacyFieldsFromBlocks()
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
