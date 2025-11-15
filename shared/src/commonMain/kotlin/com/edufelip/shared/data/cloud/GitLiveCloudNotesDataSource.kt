package com.edufelip.shared.data.cloud

import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.toJson
import com.edufelip.shared.domain.model.toSummary
import com.edufelip.shared.domain.model.withFallbacks
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.firestoreSettings
import dev.gitlive.firebase.firestore.memoryCacheSettings
import dev.gitlive.firebase.firestore.memoryEagerGcSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

internal object GitLiveCloudNotesDataSource : CloudNotesDataSource {
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
        val notesFromSubcollections = notesCollection(uid)
            .get()
            .documents
            .mapNotNull { it.toNoteOrNull() }
        val foldersFromSubcollections = foldersCollection(uid)
            .get()
            .documents
            .mapNotNull { it.toFolderOrNull() }
        if (notesFromSubcollections.isNotEmpty() || foldersFromSubcollections.isNotEmpty()) {
            return RemoteSyncPayload(
                notesFromSubcollections.sortedBy { it.updatedAt },
                foldersFromSubcollections.sortedBy { it.updatedAt },
            )
        }

        val root = Firebase.firestore.collection("users").document(uid).get()
        val userDoc = runCatching { root.data(UserDoc.serializer()) }
            .onFailure { throwable ->
                logDecodeError("user", uid, throwable)
            }
            .getOrNull()
        if (userDoc == null) {
            return RemoteSyncPayload(emptyList(), emptyList())
        }

        val nestedNotes = userDoc.notes.mapNotNull { (entryId, doc) ->
            doc.toDomainNote(entryId)
        }
        val nestedFolders = userDoc.folders.mapNotNull { (entryId, doc) ->
            doc.toDomainFolder(entryId)
        }
        return RemoteSyncPayload(
            nestedNotes.sortedBy { it.updatedAt },
            nestedFolders.sortedBy { it.updatedAt },
        )
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
            if (createdAt == 0L) FieldValue.serverTimestamp else createdAt.toFirestoreTimestamp(),
        )
        put(
            "updatedAt",
            if (useServerUpdatedAt) FieldValue.serverTimestamp else updatedAt.toFirestoreTimestamp(),
        )
        put("stableId", stableId)
    }
}

private fun Folder.toFirestoreData(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "createdAt" to if (createdAt == 0L) FieldValue.serverTimestamp else createdAt.toFirestoreTimestamp(),
    "updatedAt" to FieldValue.serverTimestamp,
)

private fun Long.toFirestoreTimestamp(): Timestamp = Timestamp(
    seconds = this / 1000,
    nanoseconds = ((this % 1000) * 1_000_000).toInt(),
)
internal object GitLiveCurrentUserProvider : CurrentUserProvider {
    override val uid: Flow<String?> =
        Firebase.auth.authStateChanged.map { user -> user?.uid }
}
