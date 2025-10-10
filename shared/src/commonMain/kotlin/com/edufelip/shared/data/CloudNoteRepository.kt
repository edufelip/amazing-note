package com.edufelip.shared.data

import com.edufelip.shared.cloud.CloudNotesDataSource
import com.edufelip.shared.cloud.CurrentUserProvider
import com.edufelip.shared.cloud.provideCloudNotesDataSource
import com.edufelip.shared.cloud.provideCurrentUserProvider
import com.edufelip.shared.domain.repository.NoteRepository
import com.edufelip.shared.model.Folder
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import com.edufelip.shared.util.nowEpochMs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class CloudNoteRepository(
    private val cloud: CloudNotesDataSource = provideCloudNotesDataSource(),
    private val currentUser: CurrentUserProvider = provideCurrentUserProvider(),
) : NoteRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun notes(): Flow<List<Note>> = currentUser.uid.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList()) else cloud.observe(uid)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun trash(): Flow<List<Note>> = notes().flatMapLatest { list -> flowOf(list.filter { it.deleted }) }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun notesByFolder(folderId: Long): Flow<List<Note>> = notes().flatMapLatest { list -> flowOf(list.filter { it.folderId == folderId }) }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun notesWithoutFolder(): Flow<List<Note>> = notes().flatMapLatest { list -> flowOf(list.filter { it.folderId == null }) }

    override fun folders(): Flow<List<Folder>> = flowOf(emptyList())

    override suspend fun insert(title: String, priority: Priority, description: String, folderId: Long?) {
        val uid = currentUser.uid.first() ?: return
        val now = nowEpochMs()
        val note = Note(
            id = now.hashCode(),
            title = title,
            priority = priority,
            description = description,
            deleted = false,
            createdAt = now,
            updatedAt = now,
            dirty = false,
            localUpdatedAt = now,
            folderId = folderId,
        )
        cloud.upsert(uid, note)
    }

    override suspend fun update(
        id: Int,
        title: String,
        priority: Priority,
        description: String,
        deleted: Boolean,
        folderId: Long?,
    ) {
        val uid = currentUser.uid.first() ?: return
        val now = nowEpochMs()
        val note = Note(
            id = id,
            title = title,
            priority = priority,
            description = description,
            deleted = deleted,
            createdAt = now,
            updatedAt = now,
            dirty = true,
            localUpdatedAt = now,
            folderId = folderId,
        )
        cloud.upsert(uid, note)
    }

    override suspend fun setDeleted(id: Int, deleted: Boolean) {
        val uid = currentUser.uid.first() ?: return
        val now = nowEpochMs()
        cloud.upsert(
            uid,
            Note(
                id,
                "",
                Priority.MEDIUM,
                "",
                deleted,
                createdAt = now,
                updatedAt = now,
                dirty = false,
                localUpdatedAt = now,
            ),
        )
    }

    override suspend fun delete(id: Int) {
        val uid = currentUser.uid.first() ?: return
        cloud.delete(uid, id)
    }

    override suspend fun assignToFolder(id: Int, folderId: Long?) {
        // Folders are a local-only concept for now on cloud repository.
    }

    override suspend fun insertFolder(name: String): Long = 0L

    override suspend fun renameFolder(id: Long, name: String) {}

    override suspend fun deleteFolder(id: Long) {}
}
