package com.edufelip.shared.sync

import com.edufelip.shared.data.cloud.CloudNotesDataSource
import com.edufelip.shared.data.cloud.CurrentUserProvider
import com.edufelip.shared.data.cloud.provideCloudNotesDataSource
import com.edufelip.shared.data.cloud.provideCurrentUserProvider
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.attachmentsFromJson
import com.edufelip.shared.domain.model.blocksFromJson
import com.edufelip.shared.domain.model.blocksToJson
import com.edufelip.shared.domain.model.ensureBlocks
import com.edufelip.shared.domain.model.spansFromJson
import com.edufelip.shared.domain.model.toJson
import com.edufelip.shared.domain.model.withLegacyFieldsFromBlocks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class NotesSyncManager(
    private val db: NoteDatabase,
    private val scope: CoroutineScope,
    private val cloud: CloudNotesDataSource = provideCloudNotesDataSource(),
    private val currentUser: CurrentUserProvider = provideCurrentUserProvider(),
) {
    private var cloudJob: Job? = null
    private var lastUid: String? = null

    // Throttle: after 3 consecutive merges, pause further merges until next explicit sync
    private var mergeCallCount: Int = 0
    private var mergingDisabled: Boolean = false
    private var storedRemoteHash: Long? = null
    private val _events = MutableSharedFlow<SyncEvent>(
        replay = 1,
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<SyncEvent> = _events

    init {
        _events.tryEmit(SyncEvent.SyncCompleted)
    }

    fun start() {
        cloudJob?.cancel()
        scope.launch {
            currentUser.uid.collect { uid ->
                cloudJob?.cancel()
                if (uid != lastUid) {
                    if (lastUid != null) clearLocal()
                    lastUid = uid
                    resetMergeThrottle()
                }
                if (uid != null) {
                    cloudJob = cloud.observe(uid).onEach { remote ->
                        mergeRemoteIntoLocalAndPushLocalNewer(uid, remote)
                    }.launchIn(scope)
                }
            }
        }
    }

    suspend fun syncNow() {
        val uid = currentUser.uid.first() ?: return
        resetMergeThrottle()
        val remote = cloud.getAll(uid)
        mergeRemoteIntoLocalAndPushLocalNewer(uid, remote)
    }

    suspend fun syncLocalToRemoteOnly() {
        val uid = currentUser.uid.first() ?: return
        // Push only dirty rows to avoid unnecessary writes and reordering
        val dirtyRows = db.noteQueries.selectDirty().executeAsList()
        if (dirtyRows.isEmpty()) {
            _events.tryEmit(SyncEvent.SyncCompleted)
            return
        }
        for (row in dirtyRows) {
            val note = Note(
                id = row.id.toInt(),
                title = row.title,
                description = row.description,
                deleted = row.deleted != 0L,
                createdAt = row.created_at,
                updatedAt = row.updated_at,
                dirty = row.local_dirty != 0L,
                localUpdatedAt = row.local_updated_at,
                folderId = row.folder_id,
                blocks = blocksFromJson(row.blocks),
                descriptionSpans = spansFromJson(row.description_spans),
                attachments = attachmentsFromJson(row.attachments),
            ).ensureBlocks().withLegacyFieldsFromBlocks()
            cloud.upsertPreserveUpdatedAt(uid, note)
            db.noteQueries.clearDirtyById(row.id)
        }
        _events.tryEmit(SyncEvent.SyncCompleted)
    }

    private suspend fun mergeRemoteIntoLocalAndPushLocalNewer(uid: String, remote: List<Note>) {
        val currentRemoteHash = listHash(remote)
        if (mergingDisabled) {
            if (storedRemoteHash != null && currentRemoteHash == storedRemoteHash) return
            resetMergeThrottle()
        }

        val localAll = getAllLocal()
        val localHash = listHash(localAll)
        if (currentRemoteHash == localHash) {
            resetMergeThrottle()
            _events.tryEmit(SyncEvent.SyncCompleted)
            return
        }

        mergeCallCount += 1
        val disableAfterThisCall = mergeCallCount >= 3
        val remoteById = remote.associateBy { it.id }
        val localById = localAll.associateBy { it.id }
        var overwrites = 0
        val toPushDistinct = LinkedHashMap<Int, Note>()

        for ((id, r) in remoteById) {
            val l = localById[id]
            if (l == null) {
                insertLocal(r)
            } else {
                when {
                    r.updatedAt > l.updatedAt -> {
                        updateLocalFromRemote(l.id, r)
                        overwrites++
                    }

                    l.updatedAt > r.updatedAt -> toPushDistinct[id] = l
                }
            }
        }

        val remoteIds = remoteById.keys
        for ((id, l) in localById) {
            if (id !in remoteIds) {
                toPushDistinct[id] = l
            }
        }

        for ((_, note) in toPushDistinct) cloud.upsert(uid, note)

        if (overwrites > 0) _events.tryEmit(SyncEvent.OverwritesApplied(overwrites))
        _events.tryEmit(SyncEvent.SyncCompleted)
        if (disableAfterThisCall) {
            mergingDisabled = true
            storedRemoteHash = currentRemoteHash
        }
    }

    private fun getAllLocal(): List<Note> {
        val rowsActive = db.noteQueries.selectAll().executeAsList()
        val rowsDeleted = db.noteQueries.selectDeleted().executeAsList()
        fun mapRow(row: com.edufelip.shared.db.Note): Note = Note(
            id = row.id.toInt(),
            title = row.title,
            description = row.description,
            deleted = row.deleted != 0L,
            createdAt = row.created_at,
            updatedAt = row.updated_at,
            dirty = row.local_dirty != 0L,
            localUpdatedAt = row.local_updated_at,
            folderId = row.folder_id,
            descriptionSpans = spansFromJson(row.description_spans),
            attachments = attachmentsFromJson(row.attachments),
            blocks = blocksFromJson(row.blocks),
        ).ensureBlocks()
        return rowsActive.map(transform = ::mapRow) + rowsDeleted.map(::mapRow)
    }

    private fun clearLocal() {
        db.noteQueries.deleteAll()
        db.noteQueries.deleteAllFolders()
        _events.tryEmit(SyncEvent.SyncCompleted)
    }

    private fun insertLocal(n: Note) {
        val normalized = n.withLegacyFieldsFromBlocks()
        db.noteQueries.insertWithId(
            id = normalized.id.toLong(),
            title = normalized.title,
            description = normalized.description,
            description_spans = normalized.descriptionSpans.toJson(),
            attachments = normalized.attachments.toJson(),
            blocks = normalized.blocks.blocksToJson(),
            deleted = if (normalized.deleted) 1 else 0,
            created_at = normalized.createdAt,
            updated_at = normalized.updatedAt,
            folder_id = normalized.folderId,
        )
    }

    private fun updateLocalFromRemote(id: Int, note: Note) {
        val normalized = note.withLegacyFieldsFromBlocks()
        db.noteQueries.updateFromRemote(
            title = normalized.title,
            description = normalized.description,
            description_spans = normalized.descriptionSpans.toJson(),
            attachments = normalized.attachments.toJson(),
            blocks = normalized.blocks.blocksToJson(),
            deleted = if (normalized.deleted) 1 else 0,
            updated_at = normalized.updatedAt,
            folder_id = normalized.folderId,
            id = id.toLong(),
        )
    }

    private fun resetMergeThrottle() {
        mergeCallCount = 0
        mergingDisabled = false
        storedRemoteHash = null
    }

    private fun listHash(list: List<Note>): Long {
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
            mixString(n.title)
            mixString(n.description)
            n.descriptionSpans.forEach { span ->
                mix(span.start.toLong())
                mix(span.end.toLong())
                mixString(span.style.name)
            }
            n.attachments.forEach { attachment ->
                mixString(attachment.id)
                mixString(attachment.downloadUrl)
                mixString(attachment.thumbnailUrl ?: "")
                mixString(attachment.mimeType)
            }
            n.blocks.sortedBy { it.order }.forEach { block ->
                mixString(block.id)
                mixString(block.type.name)
                mixString(block.content)
                block.metadata.entries
                    .sortedBy { it.key }
                    .forEach { (key, value) ->
                        mixString(key)
                        mixString(value)
                    }
                mix(block.order.toLong())
            }
            mix(if (n.deleted) 1 else 0)
            mix(n.folderId ?: -1L)
        }
        return hash
    }
}

sealed class SyncEvent {
    data class OverwritesApplied(val count: Int) : SyncEvent()
    data object SyncCompleted : SyncEvent()
}
