package com.edufelip.amazing_note.domain

import com.edufelip.shared.domain.repository.NoteRepository
import com.edufelip.shared.model.Folder
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeDomainRepository : NoteRepository {
    private val notes = mutableListOf<Note>()
    private val state = MutableStateFlow<List<Note>>(emptyList())
    private val folders = mutableListOf<Folder>()
    private val folderState = MutableStateFlow<List<Folder>>(emptyList())

    override fun notes(): Flow<List<Note>> = state.map { list -> list.filter { !it.deleted } }

    override fun trash(): Flow<List<Note>> = state.map { list -> list.filter { it.deleted } }

    override fun notesByFolder(folderId: Long): Flow<List<Note>> = state.map { list -> list.filter { !it.deleted && it.folderId == folderId } }

    override fun notesWithoutFolder(): Flow<List<Note>> = state.map { list -> list.filter { !it.deleted && it.folderId == null } }

    override fun folders(): Flow<List<Folder>> = folderState

    override suspend fun insert(title: String, priority: Priority, description: String, folderId: Long?) {
        val nextId = (notes.maxOfOrNull { it.id } ?: 0) + 1
        val now = System.currentTimeMillis()
        notes += Note(nextId, title, priority, description, false, createdAt = now, updatedAt = now, folderId = folderId)
        state.value = notes.toList()
    }

    override suspend fun update(
        id: Int,
        title: String,
        priority: Priority,
        description: String,
        deleted: Boolean,
        folderId: Long?,
    ) {
        val idx = notes.indexOfFirst { it.id == id }.takeIf { it >= 0 } ?: return
        val now = System.currentTimeMillis()
        notes[idx] = notes[idx].copy(
            title = title,
            priority = priority,
            description = description,
            deleted = deleted,
            updatedAt = now,
            folderId = folderId,
        )
        state.value = notes.toList()
    }

    override suspend fun setDeleted(id: Int, deleted: Boolean) {
        val idx = notes.indexOfFirst { it.id == id }.takeIf { it >= 0 } ?: return
        val now = System.currentTimeMillis()
        notes[idx] = notes[idx].copy(deleted = deleted, updatedAt = now)
        state.value = notes.toList()
    }

    override suspend fun delete(id: Int) {
        val idx = notes.indexOfFirst { it.id == id }.takeIf { it >= 0 } ?: return
        notes.removeAt(idx)
        state.value = notes.toList()
    }

    override suspend fun assignToFolder(id: Int, folderId: Long?) {
        val idx = notes.indexOfFirst { it.id == id }.takeIf { it >= 0 } ?: return
        val now = System.currentTimeMillis()
        notes[idx] = notes[idx].copy(folderId = folderId, updatedAt = now)
        state.value = notes.toList()
    }

    override suspend fun insertFolder(name: String): Long {
        val nextId = (folders.maxOfOrNull { it.id } ?: 0L) + 1L
        val now = System.currentTimeMillis().toLong()
        folders += Folder(nextId, name, createdAt = now, updatedAt = now)
        folderState.value = folders.toList()
        return nextId
    }

    override suspend fun renameFolder(id: Long, name: String) {
        val idx = folders.indexOfFirst { it.id == id }.takeIf { it >= 0 } ?: return
        val now = System.currentTimeMillis().toLong()
        folders[idx] = folders[idx].copy(name = name, updatedAt = now)
        folderState.value = folders.toList()
    }

    override suspend fun deleteFolder(id: Long) {
        folders.removeAll { it.id == id }
        folderState.value = folders.toList()
        val now = System.currentTimeMillis()
        notes.replaceAll { note ->
            if (note.folderId == id) note.copy(folderId = null, updatedAt = now) else note
        }
        state.value = notes.toList()
    }
}
