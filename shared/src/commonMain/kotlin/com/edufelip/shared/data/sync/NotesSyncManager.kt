package com.edufelip.shared.data.sync

import com.edufelip.shared.core.time.nowEpochMs
import com.edufelip.shared.data.cloud.CloudNotesDataSource
import com.edufelip.shared.data.cloud.CurrentUserProvider
import com.edufelip.shared.data.cloud.RemoteSyncPayload
import com.edufelip.shared.data.cloud.provideCloudNotesDataSource
import com.edufelip.shared.data.cloud.provideCurrentUserProvider
import com.edufelip.shared.data.db.decryptField
import com.edufelip.shared.data.db.encryptField
import com.edufelip.shared.data.storage.RemoteAttachmentStorage
import com.edufelip.shared.data.storage.provideRemoteAttachmentStorage
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.TextBlock
import com.edufelip.shared.domain.model.attachmentsFromJson
import com.edufelip.shared.domain.model.noteContentFromJson
import com.edufelip.shared.domain.model.spansFromJson
import com.edufelip.shared.domain.model.toJson
import com.edufelip.shared.domain.model.toSummary
import com.edufelip.shared.domain.model.withFallbacks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class NotesSyncManager(
    private val db: NoteDatabase,
    private val scope: CoroutineScope,
    private val cloud: CloudNotesDataSource = provideCloudNotesDataSource(),
    private val currentUser: CurrentUserProvider = provideCurrentUserProvider(),
    private val attachmentStorage: RemoteAttachmentStorage = provideRemoteAttachmentStorage(),
) {
    private var lastUid: String? = null
    private var lastRemoteHash: Long? = null

    private var mergingDisabled: Boolean = false
    private var storedRemoteHash: Long? = null
    private val _events = MutableSharedFlow<SyncEvent>(
        replay = 1,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<SyncEvent> = _events
    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing.asStateFlow()

    fun start() {
        scope.launch {
            currentUser.uid.collect { uid ->
                if (uid != lastUid) {
                    if (lastUid != null) clearLocal()
                    lastUid = uid
                    resetMergeThrottle()
                    lastRemoteHash = null
                }
                if (uid != null) {
                    mergeRemoteIntoLocalAndPushLocalNewer(uid, cloud.getAll(uid))
                }
            }
        }
    }

    suspend fun syncNow(uid: String? = null) {
        val uid = uid ?: awaitCurrentUid() ?: return
        println("SYNC STARTED 1")
        emitSyncStarted()
        resetMergeThrottle()
        val payload = runCatching { cloud.getAll(uid) }
            .onFailure { throwable ->
                handleSyncError("manual sync", throwable)
            }
            .getOrNull() ?: run {
                emitSyncFailed("Sync aborted: no data returned")
                return
            }
        if (payload.notes.isEmpty() && payload.folders.isEmpty()) {
            emitSyncFailed("Sync skipped: no remote data")
            return
        }
        mergeRemoteIntoLocalAndPushLocalNewer(uid, payload)
    }

    suspend fun syncLocalToRemoteOnly() {
        val uid = awaitCurrentUid() ?: return
        println("SYNC STARTED 2")
        emitSyncStarted()
        val pushedSomething = pushPendingFolderDeletions(uid) || pushPendingNoteDeletions(uid) || pushDirtyFoldersNow(uid)
        // Push only dirty rows to avoid unnecessary writes and reordering
        val dirtyRows = db.noteQueries.selectDirty().executeAsList()
        if (dirtyRows.isEmpty() && !pushedSomething) {
            emitSyncCompleted()
            return
        }
        for (row in dirtyRows) {
            val note = rowToNote(row)
            cloud.upsertPreserveUpdatedAt(uid, note)
            db.noteQueries.clearDirtyById(row.id)
        }
        emitSyncCompleted()
    }

    private suspend fun pushDirtyFoldersNow(uid: String): Boolean {
        val dirtyFolders = db.noteQueries.selectDirtyFolders().executeAsList()
        if (dirtyFolders.isEmpty()) return false
        for (row in dirtyFolders) {
            val folder = rowToFolder(row)
            if (folder.deleted) {
                runCatching {
                    cloud.deleteFolder(uid, folder.id)
                    db.noteQueries.deleteFolder(folder.id)
                }.onFailure {
                    logSyncError("Failed to delete folder ${folder.id}", it)
                }
            } else {
                runCatching {
                    cloud.upsertFolder(uid, folder)
                    db.noteQueries.clearFolderDirtyById(folder.id)
                }.onFailure {
                    logSyncError("Failed to upsert folder ${folder.id}", it)
                }
            }
        }
        return true
    }

    private suspend fun awaitCurrentUid(): String? = currentUser.uid.filterNotNull().firstOrNull()

    private suspend fun pushPendingNoteDeletions(uid: String): Boolean {
        val pending = db.noteQueries.selectPendingNoteDeletions().executeAsList()
        if (pending.isEmpty()) return false
        var pushed = false
        for (entry in pending) {
            val noteId = entry.id.toInt()
            val firestoreResult = runCatching { cloud.delete(uid, noteId) }
            if (firestoreResult.isFailure) {
                logSyncError("Failed to delete remote note $noteId", firestoreResult.exceptionOrNull())
                continue
            }
            val storagePaths = decodeStoragePaths(entry.storage_paths)
            val storageResult = runCatching {
                if (storagePaths.isNotEmpty()) {
                    attachmentStorage.deleteNoteAttachments(storagePaths)
                }
            }
            if (storageResult.isFailure) {
                logSyncError("Failed to delete storage for note $noteId", storageResult.exceptionOrNull())
                continue
            }
            db.noteQueries.deletePendingNoteDeletionById(entry.id)
            pushed = true
        }
        return pushed
    }

    private suspend fun pushPendingFolderDeletions(uid: String): Boolean {
        val pending = db.noteQueries.selectPendingFolderDeletions().executeAsList()
        if (pending.isEmpty()) return false
        var pushed = false
        for (entry in pending) {
            val folderId = entry.id
            val result = runCatching { cloud.deleteFolder(uid, folderId) }
            if (result.isSuccess) {
                db.noteQueries.deletePendingFolderDeletionById(folderId)
                db.noteQueries.deleteFolder(folderId)
                pushed = true
            } else {
                logSyncError("Failed to delete remote folder $folderId", result.exceptionOrNull())
            }
        }
        return pushed
    }

    private suspend fun mergeRemoteIntoLocalAndPushLocalNewer(uid: String, payload: RemoteSyncPayload) {
        var syncStarted = false
        fun ensureSyncStarted() {
            if (!syncStarted) {
                println("SYNC STARTED 3 ")
                emitSyncStarted()
                syncStarted = true
            }
        }
        val pushedFolders = pushPendingFolderDeletions(uid)
        val pushedNotes = pushPendingNoteDeletions(uid)
        val pushedPending = pushedFolders || pushedNotes
        if (pushedPending) ensureSyncStarted()
        val remoteNotes = payload.notes
        val remoteFolders = payload.folders
        val currentRemoteHash = combinedHash(remoteNotes, remoteFolders)
        if (!pushedPending && lastRemoteHash != null && currentRemoteHash == lastRemoteHash) return
        if (mergingDisabled) {
            if (storedRemoteHash != null && currentRemoteHash == storedRemoteHash) return
            resetMergeThrottle()
        }

        val localNotes = getAllLocalNotes()
        val localFolders = getAllLocalFolders()
        val localHash = combinedHash(localNotes, localFolders)
        if (!pushedPending && currentRemoteHash == localHash) {
            lastRemoteHash = currentRemoteHash
            return
        }

        ensureSyncStarted()
        val foldersToPush = mergeRemoteFolders(remoteFolders, localFolders)
        val (notesToPush, overwrites) = mergeRemoteNotes(remoteNotes, localNotes)

        pushFolders(uid, foldersToPush.values)
        for (note in notesToPush.values) {
            cloud.upsert(uid, note)
            db.noteQueries.clearDirtyById(note.id.toLong())
        }

        if (overwrites > 0) _events.tryEmit(SyncEvent.OverwritesApplied(overwrites))
        lastRemoteHash = currentRemoteHash
        if (syncStarted) emitSyncCompleted()
        mergingDisabled = true
        storedRemoteHash = currentRemoteHash
    }

    private fun getAllLocalNotes(): List<Note> {
        val rowsActive = db.noteQueries.selectAll().executeAsList()
        val rowsDeleted = db.noteQueries.selectDeleted().executeAsList()
        return rowsActive.map(::rowToNote) + rowsDeleted.map(::rowToNote)
    }

    private fun getAllLocalFolders(): List<Folder> {
        val rows = db.noteQueries.selectAllFolders().executeAsList()
        return rows.map(::rowToFolder)
    }

    private fun mergeRemoteFolders(
        remote: List<Folder>,
        localAll: List<Folder>,
    ): LinkedHashMap<Long, Folder> {
        val remoteById = remote.associateBy { it.id }
        val localById = localAll.associateBy { it.id }
        val toPushDistinct = LinkedHashMap<Long, Folder>()
        for ((id, remoteFolder) in remoteById) {
            val local = localById[id]
            if (local == null) {
                insertLocalFolder(remoteFolder)
                continue
            }
            if (local.deleted) {
                toPushDistinct[id] = local
                continue
            }
            when {
                remoteFolder.updatedAt > local.updatedAt -> updateLocalFolderFromRemote(remoteFolder)
                local.updatedAt > remoteFolder.updatedAt && local.dirty -> toPushDistinct[id] = local
            }
        }

        val remoteIds = remoteById.keys
        for ((id, local) in localById) {
            if (id !in remoteIds) {
                if (local.dirty || local.deleted) {
                    toPushDistinct[id] = local
                } else {
                    deleteLocalFolder(id)
                }
            }
        }
        return toPushDistinct
    }

    private fun mergeRemoteNotes(
        remote: List<Note>,
        localAll: List<Note>,
    ): Pair<LinkedHashMap<Int, Note>, Int> {
        val remoteById = remote.associateBy { it.id }
        val localById = localAll.associateBy { it.id }
        val toPushDistinct = LinkedHashMap<Int, Note>()
        var overwrites = 0

        for ((id, remoteNote) in remoteById) {
            ensureFolderIfReferenced(remoteNote)
            val local = localById[id]
            if (local == null) {
                insertLocalNote(remoteNote)
            } else {
                when {
                    remoteNote.updatedAt > local.updatedAt -> {
                        updateLocalNoteFromRemote(local.id, remoteNote)
                        overwrites += 1
                    }

                    local.updatedAt > remoteNote.updatedAt -> toPushDistinct[id] = local
                }
            }
        }

        val remoteIds = remoteById.keys
        for ((id, local) in localById) {
            if (id !in remoteIds) {
                if (local.dirty) {
                    toPushDistinct[id] = local
                } else {
                    deleteLocalNote(id)
                }
            }
        }

        return toPushDistinct to overwrites
    }

    private fun ensureFolderIfReferenced(note: Note) {
        val folderId = note.folderId ?: return
        ensureFolderExists(folderId, note.updatedAt)
    }

    private fun ensureFolderExists(folderId: Long, fallbackTimestamp: Long) {
        val existing = db.noteQueries.selectFolderById(folderId).executeAsOneOrNull()
        if (existing != null) return
        val timestamp = if (fallbackTimestamp > 0) fallbackTimestamp else nowEpochMs()
        db.noteQueries.insertFolderWithId(
            id = folderId,
            name = PLACEHOLDER_FOLDER_NAME,
            created_at = timestamp,
            updated_at = timestamp,
            deleted = 0,
            local_dirty = 1,
            local_updated_at = timestamp,
        )
    }

    private fun clearLocal() {
        db.noteQueries.deleteAll()
        db.noteQueries.deleteAllFolders()
        db.noteQueries.deleteAllPendingNoteDeletions()
        db.noteQueries.deleteAllPendingFolderDeletions()
        emitSyncCompleted()
    }

    private fun rowToNote(row: com.edufelip.shared.db.Note): Note {
        val title = decryptField(row.title)
        val description = decryptField(row.description)
        val spans = spansFromJson(decryptField(row.description_spans))
        val attachments = attachmentsFromJson(decryptField(row.attachments))
        val contentJson = row.content_json?.let(::decryptField)
        val content = noteContentFromJson(contentJson)
        val summary = content.toSummary().withFallbacks(description, spans, attachments)
        val stableId = row.stable_id.takeIf { it.isNotBlank() } ?: row.id.toString()
        return Note(
            id = row.id.toInt(),
            stableId = stableId,
            title = title,
            description = summary.description,
            deleted = row.deleted != 0L,
            createdAt = row.created_at,
            updatedAt = row.updated_at,
            dirty = row.local_dirty != 0L,
            localUpdatedAt = row.local_updated_at,
            folderId = row.folder_id,
            descriptionSpans = summary.spans,
            attachments = summary.attachments,
            content = content,
        )
    }

    private fun rowToFolder(row: com.edufelip.shared.db.Folder): Folder = Folder(
        id = row.id,
        name = row.name,
        createdAt = row.created_at,
        updatedAt = row.updated_at,
        deleted = row.deleted != 0L,
        dirty = row.local_dirty != 0L,
        localUpdatedAt = row.local_updated_at,
    )

    private fun insertLocalNote(note: Note) {
        val summary = note.content.toSummary().withFallbacks(note.description, note.descriptionSpans, note.attachments)
        db.noteQueries.insertWithId(
            id = note.id.toLong(),
            title = encryptField(note.title),
            description = encryptField(summary.description),
            description_spans = encryptField(summary.spans.toJson()),
            attachments = encryptField(summary.attachments.toJson()),
            blocks = "[]",
            content_json = encryptField(note.content.toJson()),
            deleted = if (note.deleted) 1 else 0,
            created_at = note.createdAt,
            updated_at = note.updatedAt,
            folder_id = note.folderId,
            stable_id = note.stableId,
        )
    }

    private fun insertLocalFolder(folder: Folder, markDirtyOverride: Boolean = false) {
        db.noteQueries.insertFolderWithId(
            id = folder.id,
            name = folder.name,
            created_at = folder.createdAt,
            updated_at = folder.updatedAt,
            deleted = if (folder.deleted) 1 else 0,
            local_dirty = if (markDirtyOverride || folder.dirty) 1 else 0,
            local_updated_at = (folder.localUpdatedAt.takeIf { it != 0L } ?: folder.updatedAt),
        )
    }

    private fun updateLocalNoteFromRemote(id: Int, note: Note) {
        val summary = note.content.toSummary().withFallbacks(note.description, note.descriptionSpans, note.attachments)
        db.noteQueries.updateFromRemote(
            title = encryptField(note.title),
            description = encryptField(summary.description),
            description_spans = encryptField(summary.spans.toJson()),
            attachments = encryptField(summary.attachments.toJson()),
            blocks = "[]",
            content_json = encryptField(note.content.toJson()),
            deleted = if (note.deleted) 1 else 0,
            updated_at = note.updatedAt,
            folder_id = note.folderId,
            stable_id = note.stableId,
            id = id.toLong(),
        )
    }

    private fun updateLocalFolderFromRemote(folder: Folder) {
        db.noteQueries.updateFolderFromRemote(
            name = folder.name,
            updated_at = folder.updatedAt,
            deleted = if (folder.deleted) 1 else 0,
            id = folder.id,
        )
    }

    private fun resetMergeThrottle() {
        mergingDisabled = false
        storedRemoteHash = null
    }

    private fun deleteLocalNote(id: Int) {
        db.noteQueries.deleteById(id.toLong())
    }

    private fun deleteLocalFolder(id: Long) {
        db.noteQueries.deleteFolder(id)
    }

    private suspend fun pushFolders(uid: String, folders: Collection<Folder>) {
        if (folders.isEmpty()) return
        for (folder in folders) {
            if (folder.deleted) {
                cloud.deleteFolder(uid, folder.id)
                deleteLocalFolder(folder.id)
            } else {
                cloud.upsertFolder(uid, folder)
                db.noteQueries.clearFolderDirtyById(folder.id)
            }
        }
    }

    private fun logSyncError(message: String, throwable: Throwable?) {
        if (throwable != null) {
            println("NotesSyncManager: $message -> ${throwable.message}")
        } else {
            println("NotesSyncManager: $message")
        }
    }

    private fun emitSyncStarted() {
        _syncing.value = true
        _events.tryEmit(SyncEvent.SyncStarted)
    }

    private fun emitSyncCompleted() {
        println("SYNC COMPLETED")
        _syncing.value = false
        _events.tryEmit(SyncEvent.SyncCompleted)
    }

    private fun emitSyncFailed(message: String) {
        println("SYNC FAILED")
        _syncing.value = false
        _events.tryEmit(SyncEvent.SyncFailed(message))
    }

    private fun handleSyncError(context: String, throwable: Throwable) {
        logSyncError("Sync failure during $context", throwable)
        val message = when {
            throwable.isPermissionDenied() -> "Sync failed: missing Firestore permissions for this account."
            else -> throwable.message ?: "Sync failed due to an unexpected error."
        }
        emitSyncFailed(message)
    }

    private fun Throwable.isPermissionDenied(): Boolean = message?.contains("PERMISSION_DENIED", ignoreCase = true) == true

    private fun combinedHash(notes: List<Note>, folders: List<Folder>): Long {
        val notesHash = noteListHash(notes)
        val foldersHash = folderListHash(folders)
        return notesHash xor foldersHash
    }

    private fun folderListHash(list: List<Folder>): Long {
        var hash = -0x340d631b7bdddcdbL
        fun mix(v: Long) {
            var x = v
            repeat(8) {
                val b = (x and 0xFF).toInt()
                hash = hash xor b.toLong()
                hash *= 0x100000001b3L
                x = x ushr 8
            }
        }

        fun mixString(s: String) {
            for (ch in s) {
                hash = hash xor ch.code.toLong()
                hash *= 0x100000001b3L
            }
            hash = hash xor 0xFF
            hash *= 0x100000001b3L
        }

        val sorted = list.sortedBy { it.id }
        for (folder in sorted) {
            mix(folder.id)
            mixString(folder.name)
            mix(folder.createdAt)
            mix(folder.updatedAt)
            mix(if (folder.deleted) 1 else 0)
        }
        return hash
    }

    private fun noteListHash(list: List<Note>): Long {
        // Stable FNV-1a 64-bit over sorted content
        var hash = -0x340d631b7bdddcdbL // FNV offset basis for 64-bit
        fun mix(v: Long) {
            var x = v
            repeat(8) {
                val b = (x and 0xFF).toInt()
                hash = hash xor b.toLong()
                hash *= 0x100000001b3L // FNV prime 64
                x = x ushr 8
            }
        }

        fun mixString(s: String) {
            for (ch in s) {
                hash = hash xor ch.code.toLong()
                hash *= 0x100000001b3L
            }
            hash = hash xor 0xFF
            hash *= 0x100000001b3L
        }
        // Canonicalize order strictly by id so hashing is order-independent
        val sorted = list.sortedBy { it.id }
        for (n in sorted) {
            mix(n.id.toLong())
            mixString(n.stableId)
            mixString(n.title)
            mixString(n.description)
            n.descriptionSpans.forEach { span ->
                mix(span.start.toLong())
                mix(span.end.toLong())
                mixString(span.style.name)
            }
            n.attachments.forEach { attachment ->
                mixString(attachment.id)
                val storagePath = attachment.storagePath ?: attachment.downloadUrl
                val thumbPath = attachment.thumbnailStoragePath ?: attachment.thumbnailUrl ?: ""
                mixString(storagePath)
                mixString(thumbPath)
                mixString(attachment.mimeType)
            }
            n.content.blocks.forEachIndexed { index, block ->
                mixString(block.id)
                when (block) {
                    is TextBlock -> {
                        mixString("text")
                        mixString(block.text)
                        block.spans.forEach { span ->
                            mix(span.start.toLong())
                            mix(span.end.toLong())
                            mixString(span.style.name)
                        }
                    }

                    is ImageBlock -> {
                        mixString("image")
                        mixString(block.storagePath ?: "")
                        mixString(block.thumbnailStoragePath ?: "")
                        mix(block.metadata.width?.toLong() ?: block.width?.toLong() ?: -1L)
                        mix(block.metadata.height?.toLong() ?: block.height?.toLong() ?: -1L)
                        mix(block.metadata.fileSizeBytes ?: -1L)
                        mixString(block.alt ?: "")
                        mixString(block.mimeType ?: block.metadata.mimeType ?: "")
                        mixString(block.fileName ?: "")
                        mixString(block.syncState.name)
                    }
                }
                mix(index.toLong())
            }
            mix(if (n.deleted) 1 else 0)
            mix(n.folderId ?: -1L)
        }
        return hash
    }
}

private fun decodeStoragePaths(raw: String?): List<String> = raw
    ?.takeIf { it.isNotBlank() }
    ?.let { runCatching { storagePathsJson.decodeFromString<List<String>>(it) }.getOrDefault(emptyList()) }
    ?: emptyList()

private val storagePathsJson = Json { ignoreUnknownKeys = true }

private const val PLACEHOLDER_FOLDER_NAME = "Untitled Folder"

sealed class SyncEvent {
    data object SyncStarted : SyncEvent()
    data class OverwritesApplied(val count: Int) : SyncEvent()
    data object SyncCompleted : SyncEvent()
    data class SyncFailed(val message: String) : SyncEvent()
}
