package com.edufelip.shared.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.domain.repository.NoteRepository
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

    override fun notes(): Flow<List<Note>> = queries.selectAll().asFlow().mapToList(dispatcher).map { rows ->
        rows.map { row ->
            Note(
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
        }
    }

    override fun trash(): Flow<List<Note>> = queries.selectDeleted().asFlow().mapToList(dispatcher).map { rows ->
        rows.map { row ->
            Note(
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
        }
    }

    override suspend fun insert(title: String, priority: Priority, description: String) {
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
        )
    }

    override suspend fun update(
        id: Int,
        title: String,
        priority: Priority,
        description: String,
        deleted: Boolean,
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
            id = id.toLong(),
        )
    }

    override suspend fun setDeleted(id: Int, deleted: Boolean) {
        val now = currentTimeMillis()
        queries.setDeleted(
            deleted = if (deleted) 1 else 0,
            updated_at = now,
            id = id.toLong(),
        )
    }

    override suspend fun delete(id: Int) {
        queries.deleteById(id.toLong())
    }
}
