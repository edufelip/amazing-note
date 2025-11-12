package com.edufelip.shared.data.cloud

import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.attachmentsFromJson
import com.edufelip.shared.domain.model.noteContentFromJson
import com.edufelip.shared.domain.model.spansFromJson
import com.edufelip.shared.domain.model.toJson
import com.edufelip.shared.domain.model.toSummary
import com.edufelip.shared.domain.model.withFallbacks
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

interface CloudNotesDataSource {
    fun observe(uid: String): Flow<RemoteSyncPayload>
    suspend fun getAll(uid: String): RemoteSyncPayload
    suspend fun upsert(uid: String, note: Note)
    suspend fun delete(uid: String, id: Int)

    // Upsert that preserves provided updatedAt (no server timestamp). Useful for push-only sync to avoid reordering.
    suspend fun upsertPreserveUpdatedAt(uid: String, note: Note)
    suspend fun upsertFolder(uid: String, folder: Folder)
    suspend fun deleteFolder(uid: String, id: Long)
}

data class RemoteSyncPayload(
    val notes: List<Note>,
    val folders: List<Folder>,
)

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

    override fun observe(uid: String): Flow<RemoteSyncPayload> {
        val notesFlow = notesCollection(uid)
            .snapshots
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { document -> document.toNoteOrNull() }
                    .sortedBy { it.updatedAt }
            }
        val foldersFlow = foldersCollection(uid)
            .snapshots
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { it.toFolderOrNull() }
                    .sortedBy { it.updatedAt }
            }
        return combine(notesFlow, foldersFlow) { notes, folders ->
            RemoteSyncPayload(notes, folders)
        }
    }

    override suspend fun getAll(uid: String): RemoteSyncPayload {
        val notes = notesCollection(uid)
            .get()
            .documents
            .mapNotNull { it.toNoteOrNull() }
            .sortedBy { it.updatedAt }
        val folders = foldersCollection(uid)
            .get()
            .documents
            .mapNotNull { it.toFolderOrNull() }
            .sortedBy { it.updatedAt }
        return RemoteSyncPayload(notes, folders)
    }

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

    override suspend fun upsertFolder(uid: String, folder: Folder) {
        val data = folder.toFirestoreData()
        foldersCollection(uid)
            .document(folder.id.toString())
            .set(data, merge = true)
    }

    override suspend fun deleteFolder(uid: String, id: Long) {
        foldersCollection(uid)
            .document(id.toString())
            .delete()
    }
}

private fun notesCollection(uid: String) = Firebase.firestore
    .collection("users")
    .document(uid)
    .collection("notes")

private fun foldersCollection(uid: String) = Firebase.firestore
    .collection("users")
    .document(uid)
    .collection("folders")

private fun Note.toFirestoreData(useServerUpdatedAt: Boolean): Map<String, Any?> {
    val summary = content.toSummary().withFallbacks(description, descriptionSpans, attachments)
    return buildMap {
        put("id", id)
        put("title", title)
        put("description", summary.description)
        put("descriptionSpans", summary.spans.toJson())
        put("attachments", summary.attachments.toJson())
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
    val content = noteContentFromJson(this["contentJson"] as? String ?: this["content_json"] as? String)
    val summary = content.toSummary().withFallbacks(description, spans, attachments)
    val note = Note(
        id = (this["id"] as? Number)?.toInt() ?: docId.toIntOrNull() ?: -1,
        title = rawTitle,
        description = summary.description,
        descriptionSpans = summary.spans,
        attachments = summary.attachments,
        content = content,
        deleted = (this["deleted"] as? Boolean) ?: false,
        createdAt = this["createdAt"].toMillis() ?: 0L,
        updatedAt = this["updatedAt"].toMillis() ?: 0L,
        folderId = (this["folderId"] as? Number)?.toLong(),
    )
    return note
}

private fun DocumentSnapshot.toFolderOrNull(): Folder? {
    val data = runCatching { data<Map<String, Any?>>() }.getOrNull() ?: return null
    val name = data["name"] as? String ?: return null
    val createdAt = data["createdAt"].toMillis() ?: 0L
    val updatedAt = data["updatedAt"].toMillis() ?: 0L
    val idValue = (data["id"] as? Number)?.toLong() ?: id.toLongOrNull() ?: return null
    return Folder(
        id = idValue,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

private fun Folder.toFirestoreData(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "createdAt" to if (createdAt == 0L) FieldValue.serverTimestamp else createdAt,
    "updatedAt" to FieldValue.serverTimestamp,
)

private fun Any?.toMillis(): Long? = when (this) {
    is Number -> toLong()
    is Timestamp -> (seconds * 1_000L) + (nanoseconds / 1_000_000L)
    else -> null
}

private object GitLiveCurrentUserProvider : CurrentUserProvider {
    override val uid: Flow<String?> =
        Firebase.auth.authStateChanged.map { user -> user?.uid }
}
