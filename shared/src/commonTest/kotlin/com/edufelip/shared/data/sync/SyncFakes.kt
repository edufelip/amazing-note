package com.edufelip.shared.data.sync

import com.edufelip.shared.data.cloud.CloudNotesDataSource
import com.edufelip.shared.data.cloud.CurrentUserProvider
import com.edufelip.shared.data.cloud.RemoteSyncPayload
import com.edufelip.shared.data.storage.RemoteAttachmentStorage
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCloudNotesDataSource : CloudNotesDataSource {
    private val remoteNotesByUser = mutableMapOf<String, MutableMap<Int, Note>>()
    private val remoteFoldersByUser = mutableMapOf<String, MutableMap<Long, Folder>>()
    private val flows = mutableMapOf<String, MutableStateFlow<RemoteSyncPayload>>()

    val upsertCalls = mutableListOf<Pair<String, Note>>()
    val deleteNoteCalls = mutableListOf<Pair<String, Int>>()
    val deleteFolderCalls = mutableListOf<Pair<String, Long>>()

    override fun observe(uid: String): Flow<RemoteSyncPayload> = flows.getOrPut(uid) {
        MutableStateFlow(snapshot(uid))
    }

    override suspend fun getAll(uid: String): RemoteSyncPayload = snapshot(uid)

    override suspend fun upsert(uid: String, note: Note) {
        val userNotes = remoteNotesByUser.getOrPut(uid) { mutableMapOf() }
        val serverNote = note.copy(updatedAt = note.updatedAt + 5, dirty = false)
        userNotes[note.id] = serverNote
        upsertCalls += uid to serverNote
        pushRemote(uid)
    }

    override suspend fun delete(uid: String, id: Int) {
        remoteNotesByUser[uid]?.remove(id)
        deleteNoteCalls += uid to id
        pushRemote(uid)
    }

    override suspend fun upsertPreserveUpdatedAt(uid: String, note: Note) {
        val userNotes = remoteNotesByUser.getOrPut(uid) { mutableMapOf() }
        userNotes[note.id] = note.copy(dirty = false)
        pushRemote(uid)
    }

    override suspend fun upsertFolder(uid: String, folder: Folder) {
        val userFolders = remoteFoldersByUser.getOrPut(uid) { mutableMapOf() }
        userFolders[folder.id] = folder.copy(dirty = false)
        pushRemote(uid)
    }

    override suspend fun deleteFolder(uid: String, id: Long) {
        remoteFoldersByUser[uid]?.remove(id)
        deleteFolderCalls += uid to id
        pushRemote(uid)
    }

    fun seedRemote(uid: String, notes: List<Note>, folders: List<Folder> = emptyList()) {
        remoteNotesByUser[uid] = notes.associateBy { it.id }.toMutableMap()
        remoteFoldersByUser[uid] = folders.associateBy { it.id }.toMutableMap()
        pushRemote(uid)
    }

    fun pushRemote(uid: String) {
        val state = flows.getOrPut(uid) { MutableStateFlow(snapshot(uid)) }
        state.value = snapshot(uid)
    }

    private fun snapshot(uid: String): RemoteSyncPayload {
        val notes = remoteNotesByUser[uid]?.values?.sortedBy { it.updatedAt } ?: emptyList()
        val folders = remoteFoldersByUser[uid]?.values?.sortedBy { it.updatedAt } ?: emptyList()
        return RemoteSyncPayload(notes, folders)
    }

    fun hasRemoteNote(uid: String, id: Int): Boolean = remoteNotesByUser[uid]?.containsKey(id) == true

    fun hasRemoteFolder(uid: String, id: Long): Boolean = remoteFoldersByUser[uid]?.containsKey(id) == true
}

class FakeCurrentUserProvider(
    initial: String? = null,
) : CurrentUserProvider {
    private val state = MutableStateFlow(initial)
    override val uid: Flow<String?> = state

    fun setCurrentUser(uid: String?) {
        state.value = uid
    }
}

class FakeRemoteAttachmentStorage : RemoteAttachmentStorage {
    val deletions = mutableListOf<List<String>>()
    var shouldFail: Boolean = false

    override suspend fun deleteNoteAttachments(paths: List<String>) {
        if (shouldFail) throw IllegalStateException("storage failure")
        deletions += paths
    }
}
