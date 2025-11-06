package com.edufelip.shared.data.sync

import com.edufelip.shared.data.cloud.CloudNotesDataSource
import com.edufelip.shared.data.cloud.CurrentUserProvider
import com.edufelip.shared.data.cloud.provideCloudNotesDataSource
import com.edufelip.shared.data.cloud.provideCurrentUserProvider
import com.edufelip.shared.data.db.decryptField
import com.edufelip.shared.data.db.encryptField
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.TextBlock
import com.edufelip.shared.domain.model.attachmentsFromJson
import com.edufelip.shared.domain.model.ensureContent
import com.edufelip.shared.domain.model.noteContentFromJson
import com.edufelip.shared.domain.model.noteContentFromLegacyBlocksJson
import com.edufelip.shared.domain.model.spansFromJson
import com.edufelip.shared.domain.model.toJson
import com.edufelip.shared.domain.model.withLegacyFieldsFromContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.collections.iterator
import kotlin.text.iterator

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
            val note = rowToNote(row)
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
                if (l.dirty) {
                    toPushDistinct[id] = l
                } else {
                    deleteLocal(id)
                }
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
        return rowsActive.map(::rowToNote) + rowsDeleted.map(::rowToNote)
    }

    private fun clearLocal() {
        db.noteQueries.deleteAll()
        db.noteQueries.deleteAllFolders()
        _events.tryEmit(SyncEvent.SyncCompleted)
    }

    private fun rowToNote(row: com.edufelip.shared.db.Note): Note {
        val title = decryptField(row.title)
        val description = decryptField(row.description)
        val spans = spansFromJson(decryptField(row.description_spans))
        val attachments = attachmentsFromJson(decryptField(row.attachments))
        val contentJson = row.content_json?.let(::decryptField)
        val parsedContent = noteContentFromJson(contentJson)
        val legacyContent = if (parsedContent.blocks.isNotEmpty()) parsedContent else noteContentFromLegacyBlocksJson(row.blocks)
        val ensuredContent = ensureContent(description, spans, attachments, legacyContent)
        return Note(
            id = row.id.toInt(),
            title = title,
            description = description,
            deleted = row.deleted != 0L,
            createdAt = row.created_at,
            updatedAt = row.updated_at,
            dirty = row.local_dirty != 0L,
            localUpdatedAt = row.local_updated_at,
            folderId = row.folder_id,
            descriptionSpans = spans,
            attachments = attachments,
            content = ensuredContent,
        ).ensureContent().withLegacyFieldsFromContent()
    }

    private fun insertLocal(n: Note) {
        val normalized = n.ensureContent().withLegacyFieldsFromContent()
        db.noteQueries.insertWithId(
            id = normalized.id.toLong(),
            title = encryptField(normalized.title),
            description = encryptField(normalized.description),
            description_spans = encryptField(normalized.descriptionSpans.toJson()),
            attachments = encryptField(normalized.attachments.toJson()),
            blocks = "[]",
            content_json = encryptField(normalized.content.toJson()),
            deleted = if (normalized.deleted) 1 else 0,
            created_at = normalized.createdAt,
            updated_at = normalized.updatedAt,
            folder_id = normalized.folderId,
        )
    }

    private fun updateLocalFromRemote(id: Int, note: Note) {
        val normalized = note.ensureContent().withLegacyFieldsFromContent()
        db.noteQueries.updateFromRemote(
            title = encryptField(normalized.title),
            description = encryptField(normalized.description),
            description_spans = encryptField(normalized.descriptionSpans.toJson()),
            attachments = encryptField(normalized.attachments.toJson()),
            blocks = "[]",
            content_json = encryptField(normalized.content.toJson()),
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

    private fun deleteLocal(id: Int) {
        db.noteQueries.deleteById(id.toLong())
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
                        mixString(block.uri)
                        mixString(block.remoteUri ?: "")
                        mix(block.width?.toLong() ?: -1L)
                        mix(block.height?.toLong() ?: -1L)
                        mixString(block.alt ?: "")
                        mixString(block.thumbnailUri ?: "")
                        mixString(block.mimeType ?: "")
                        mixString(block.fileName ?: "")
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

sealed class SyncEvent {
    data class OverwritesApplied(val count: Int) : SyncEvent()
    data object SyncCompleted : SyncEvent()
}
