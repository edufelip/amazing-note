package com.edufelip.shared.sync

import com.edufelip.shared.cloud.CloudNotesDataSource
import com.edufelip.shared.cloud.CurrentUserProvider
import com.edufelip.shared.cloud.provideCloudNotesDataSource
import com.edufelip.shared.cloud.provideCurrentUserProvider
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

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
    private val _events = MutableSharedFlow<SyncEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<SyncEvent> = _events

    fun start() {
        cloudJob?.cancel()
        cloudJob = currentUser.uid.onEach { uid ->
            // cancel handled automatically by launching new stream below
        }.launchIn(scope)

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

    suspend fun migrateLocalToCloudOnce(uid: String, isMigrated: (String) -> Boolean, setMigrated: (String) -> Unit) {
        val key = "notes_migrated_$uid"
        if (isMigrated(key)) return
        val allLocal = getAllLocal()
        for (n in allLocal) cloud.upsert(uid, n)
        setMigrated(key)
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
            priority = when (row.priority.toInt()) {
                0 -> Priority.HIGH
                1 -> Priority.MEDIUM
                else -> Priority.LOW
            },
            description = row.description,
            deleted = row.deleted != 0L,
            createdAt = row.created_at,
            updatedAt = row.updated_at,
        )
        return rowsActive.map(::mapRow) + rowsDeleted.map(::mapRow)
    }

    private fun clearLocal() {
        db.noteQueries.deleteAll()
        _events.tryEmit(SyncEvent.SyncCompleted)
    }
    private fun insertLocal(n: Note) {
        db.noteQueries.insertWithId(
            id = n.id.toLong(),
            title = n.title,
            priority = when (n.priority) {
                Priority.HIGH -> 0L
                Priority.MEDIUM -> 1L
                Priority.LOW -> 2L
            },
            description = n.description,
            deleted = if (n.deleted) 1 else 0,
            created_at = n.createdAt,
            updated_at = n.updatedAt,
        )
    }

    private fun updateLocalFromRemote(id: Int, note: Note) {
        db.noteQueries.updateNote(
            title = note.title,
            priority = when (note.priority) {
                Priority.HIGH -> 0L
                Priority.MEDIUM -> 1L
                Priority.LOW -> 2L
            },
            description = note.description,
            deleted = if (note.deleted) 1 else 0,
            updated_at = note.updatedAt,
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
            mix(n.priority.ordinal.toLong())
            mixString(n.description)
            mix(if (n.deleted) 1 else 0)
            mix(n.createdAt)
            mix(n.updatedAt)
        }
        return hash
    }
}

sealed class SyncEvent {
    data class OverwritesApplied(val count: Int) : SyncEvent()
    data object SyncCompleted : SyncEvent()
}
