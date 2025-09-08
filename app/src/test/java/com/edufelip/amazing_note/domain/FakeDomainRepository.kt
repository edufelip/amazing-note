package com.edufelip.amazing_note.domain

import com.edufelip.shared.domain.repository.NoteRepository
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeDomainRepository : NoteRepository {
    private val notes = mutableListOf<Note>()
    private val state = MutableStateFlow<List<Note>>(emptyList())

    override fun notes(): Flow<List<Note>> = state.map { list -> list.filter { !it.deleted } }

    override fun trash(): Flow<List<Note>> = state.map { list -> list.filter { it.deleted } }

    override suspend fun insert(title: String, priority: Priority, description: String) {
        val nextId = (notes.maxOfOrNull { it.id } ?: 0) + 1
        notes += Note(nextId, title, priority, description, false)
        state.value = notes.toList()
    }

    override suspend fun update(
        id: Int,
        title: String,
        priority: Priority,
        description: String,
        deleted: Boolean
    ) {
        val idx = notes.indexOfFirst { it.id == id }.takeIf { it >= 0 } ?: return
        notes[idx] = notes[idx].copy(title = title, priority = priority, description = description, deleted = deleted)
        state.value = notes.toList()
    }

    override suspend fun setDeleted(id: Int, deleted: Boolean) {
        val idx = notes.indexOfFirst { it.id == id }.takeIf { it >= 0 } ?: return
        notes[idx] = notes[idx].copy(deleted = deleted)
        state.value = notes.toList()
    }

    override suspend fun delete(id: Int) {
        val idx = notes.indexOfFirst { it.id == id }.takeIf { it >= 0 } ?: return
        notes.removeAt(idx)
        state.value = notes.toList()
    }
}

