package com.edufelip.shared.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.domain.repository.NoteRepository
import com.edufelip.shared.model.Folder
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import com.edufelip.shared.util.nowEpochMs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SqlDelightNoteRepository(
    private val database: NoteDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoteRepository {

    private val queries get() = database.noteQueries

    private fun currentTimeMillis(): Long = nowEpochMs()

    private fun mapPriority(value: Long): Priority = when (value.toInt()) {
        0 -> Priority.HIGH
        1 -> Priority.MEDIUM
        else -> Priority.LOW
    }

    private fun mapRowToNote(row: com.edufelip.shared.db.Note): Note = Note(
        id = row.id.toInt(),
        title = row.title,
        priority = mapPriority(row.priority),
        description = row.description,
        deleted = row.deleted != 0L,
        createdAt = row.created_at,
        updatedAt = row.updated_at,
        dirty = row.local_dirty != 0L,
        localUpdatedAt = row.local_updated_at,
        folderId = row.folder_id,
    )

    private fun mapRowToFolder(row: com.edufelip.shared.db.Folder): Folder = Folder(
        id = row.id,
        name = row.name,
        createdAt = row.created_at,
        updatedAt = row.updated_at,
    )

    override fun notes(): Flow<List<Note>> = queries.selectAll().asFlow().mapToList(dispatcher).map { rows -> rows.map(::mapRowToNote) }

    override fun trash(): Flow<List<Note>> = queries.selectDeleted().asFlow().mapToList(dispatcher).map { rows -> rows.map(::mapRowToNote) }

    override fun notesByFolder(folderId: Long): Flow<List<Note>> = queries.selectNotesByFolder(folder_id = folderId).asFlow().mapToList(dispatcher).map { rows -> rows.map(::mapRowToNote) }

    override fun notesWithoutFolder(): Flow<List<Note>> = queries.selectNotesWithoutFolder().asFlow().mapToList(dispatcher).map { rows -> rows.map(::mapRowToNote) }

    override fun folders(): Flow<List<Folder>> = queries.selectFolders().asFlow().mapToList(dispatcher).map { rows -> rows.map(::mapRowToFolder) }

    override suspend fun insert(title: String, priority: Priority, description: String, folderId: Long?) {
        val now = currentTimeMillis()
        queries.insertNote(
            title = title,
            priority = when (priority) {
                Priority.HIGH -> 0
                Priority.MEDIUM -> 1
                Priority.LOW -> 2
            }.toLong(),
            description = description,
            created_at = now,
            updated_at = now,
            local_updated_at = now,
            folder_id = folderId,
        )
    }

    override suspend fun update(
        id: Int,
        title: String,
        priority: Priority,
        description: String,
        deleted: Boolean,
        folderId: Long?,
    ) {
        val now = currentTimeMillis()
        queries.updateNote(
            title = title,
            priority = when (priority) {
                Priority.HIGH -> 0
                Priority.MEDIUM -> 1
                Priority.LOW -> 2
            }.toLong(),
            description = description,
            deleted = if (deleted) 1 else 0,
            updated_at = now,
            local_updated_at = now,
            folder_id = folderId,
            id = id.toLong(),
        )
    }

    override suspend fun setDeleted(id: Int, deleted: Boolean) {
        val now = currentTimeMillis()
        queries.setDeleted(
            deleted = if (deleted) 1 else 0,
            updated_at = now,
            local_updated_at = now,
            id = id.toLong(),
        )
    }

    override suspend fun delete(id: Int) {
        queries.deleteById(id.toLong())
    }

    override suspend fun assignToFolder(id: Int, folderId: Long?) {
        val now = currentTimeMillis()
        queries.setNoteFolder(
            folder_id = folderId,
            updated_at = now,
            local_updated_at = now,
            id = id.toLong(),
        )
    }

    override suspend fun insertFolder(name: String): Long {
        val now = currentTimeMillis()
        queries.insertFolder(
            name = name,
            created_at = now,
            updated_at = now,
        )
        return queries.lastInsertedFolderId().executeAsOne()
    }

    override suspend fun renameFolder(id: Long, name: String) {
        val now = currentTimeMillis()
        queries.updateFolder(
            name = name,
            updated_at = now,
            id = id,
        )
    }

    override suspend fun deleteFolder(id: Long) {
        val now = currentTimeMillis()
        queries.clearFolderAssignment(
            updated_at = now,
            local_updated_at = now,
            folder_id = id,
        )
        queries.deleteFolder(id)
    }
}
