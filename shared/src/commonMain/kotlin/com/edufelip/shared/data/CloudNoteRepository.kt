package com.edufelip.shared.data

import com.edufelip.shared.cloud.CloudNotesDataSource
import com.edufelip.shared.cloud.CurrentUserProvider
import com.edufelip.shared.cloud.provideCloudNotesDataSource
import com.edufelip.shared.cloud.provideCurrentUserProvider
import com.edufelip.shared.domain.repository.NoteRepository
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
    override fun notes(): Flow<List<Note>> =
        currentUser.uid.flatMapLatest { uid ->
            if (uid == null) flowOf(emptyList()) else cloud.observe(uid)
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun trash(): Flow<List<Note>> =
        notes().flatMapLatest { list -> flowOf(list.filter { it.deleted }) }

    override suspend fun insert(title: String, priority: Priority, description: String) {
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
        )
        cloud.upsert(uid, note)
    }

    override suspend fun update(id: Int, title: String, priority: Priority, description: String, deleted: Boolean) {
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
        )
        cloud.upsert(uid, note)
    }

    override suspend fun setDeleted(id: Int, deleted: Boolean) {
        val uid = currentUser.uid.first() ?: return
        val now = com.edufelip.shared.util.nowEpochMs()
        cloud.upsert(uid, Note(id, "", Priority.MEDIUM, "", deleted, createdAt = now, updatedAt = now))
    }

    override suspend fun delete(id: Int) {
        val uid = currentUser.uid.first() ?: return
        cloud.delete(uid, id)
    }
}

