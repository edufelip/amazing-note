package com.edufelip.shared.data.cloud

import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.normalizeCachedImages
import com.edufelip.shared.domain.model.remoteSafe
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
import dev.gitlive.firebase.storage.storage
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
                    .withResolvedStorageUrls()
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
            .withResolvedStorageUrls()
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
    val normalizedContent = content.normalizeCachedImages()
    val remoteContent = normalizedContent.remoteSafe()
    val summary = remoteContent.toSummary().withFallbacks(description, descriptionSpans, attachments)
    val remoteAttachments = summary.attachments.remoteSafe().filter { it.storagePath != null }
    return buildMap {
        put("id", id)
        put("title", title)
        put("description", summary.description)
        put("descriptionSpans", summary.spans.toJson())
        put("attachments", remoteAttachments.toJson())
        put("contentJson", remoteContent.toJson())
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

private suspend fun List<Note>.withResolvedStorageUrls(): List<Note> = map { note ->
    note.withResolvedStorageUrls()
}

private suspend fun Note.withResolvedStorageUrls(): Note {
    var mutated = false
    val updatedBlocks = content.blocks.map { block ->
        if (block is ImageBlock) {
            val resolved = block.resolveDownloadUrls()
            if (resolved !== block) mutated = true
            resolved
        } else block
    }
    if (!mutated) return this
    val updatedContent = content.copy(blocks = updatedBlocks)
    val summary = updatedContent.toSummary().withFallbacks(description, descriptionSpans, attachments)
    return copy(
        content = updatedContent,
        description = summary.description,
        descriptionSpans = summary.spans,
        attachments = summary.attachments,
    )
}

private suspend fun ImageBlock.resolveDownloadUrls(): ImageBlock {
    var newRemote: String? = null
    var newThumbRemote: String? = null
    val storage = Firebase.storage
    if (!storagePath.isNullOrBlank() && !storagePath.startsWith("http", ignoreCase = true)) {
        newRemote = runCatching { storage.reference.child(storagePath!!).getDownloadUrl() }.getOrNull()
    }
    if (!thumbnailStoragePath.isNullOrBlank() && !thumbnailStoragePath.startsWith("http", ignoreCase = true)) {
        newThumbRemote = runCatching { storage.reference.child(thumbnailStoragePath!!).getDownloadUrl() }.getOrNull()
    }
    if (newRemote == null && newThumbRemote == null) return this
    return copy(
        resolvedDownloadUrl = newRemote ?: resolvedDownloadUrl,
        resolvedThumbnailUrl = newThumbRemote ?: resolvedThumbnailUrl,
        legacyRemoteUri = newRemote ?: legacyRemoteUri,
        thumbnailUri = newThumbRemote ?: thumbnailUri,
    )
}
