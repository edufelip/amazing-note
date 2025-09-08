package com.edufelip.shared.data

import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList

class SqlDelightNoteRepository(
    private val database: NoteDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : NoteRepository {

    private val queries get() = database.noteQueries

    override fun notes(): Flow<List<Note>> =
        queries.selectAll().asFlow().mapToList(dispatcher).map { rows ->
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
                    deleted = row.deleted != 0L
                )
            }
        }

    override fun trash(): Flow<List<Note>> =
        queries.selectDeleted().asFlow().mapToList(dispatcher).map { rows ->
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
                    deleted = row.deleted != 0L
                )
            }
        }

    override suspend fun insert(title: String, priority: Priority, description: String) {
        queries.insertNote(
            title = title,
            priority = when (priority) {
                Priority.HIGH -> 0
                Priority.MEDIUM -> 1
                Priority.LOW -> 2
            }.toLong(),
            description = description
        )
    }

    override suspend fun update(
        id: Int,
        title: String,
        priority: Priority,
        description: String,
        deleted: Boolean
    ) {
        queries.updateNote(
            title = title,
            priority = when (priority) {
                Priority.HIGH -> 0
                Priority.MEDIUM -> 1
                Priority.LOW -> 2
            }.toLong(),
            description = description,
            deleted = if (deleted) 1 else 0,
            id = id.toLong()
        )
    }

    override suspend fun setDeleted(id: Int, deleted: Boolean) {
        queries.setDeleted(
            deleted = if (deleted) 1 else 0,
            id = id.toLong()
        )
    }

    override suspend fun delete(id: Int) {
        queries.deleteById(id.toLong())
    }
}
