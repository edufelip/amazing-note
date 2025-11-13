package com.edufelip.amazing_note.domain

import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.NoteAttachment
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.NoteTextSpan
import com.edufelip.shared.domain.model.generateStableNoteId
import com.edufelip.shared.domain.repository.NoteRepository
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

    override suspend fun insert(
        title: String,
        description: String,
        folderId: Long?,
        spans: List<NoteTextSpan>,
        attachments: List<NoteAttachment>,
        content: NoteContent,
        stableId: String?,
    ) {
        val nextId = (notes.maxOfOrNull { it.id } ?: 0) + 1
        val now = System.currentTimeMillis()
        val finalContent = if (content.blocks.isEmpty()) NoteContent() else content
        val resolvedStableId = stableId ?: generateStableNoteId()
        notes += Note(
            id = nextId,
            stableId = resolvedStableId,
            title = title,
            description = description,
            deleted = false,
            createdAt = now,
            updatedAt = now,
            folderId = folderId,
            descriptionSpans = spans,
            attachments = attachments,
            content = finalContent,
        )
        state.value = notes.toList()
    }

    override suspend fun update(
        id: Int,
        title: String,
        description: String,
        deleted: Boolean,
        folderId: Long?,
        spans: List<NoteTextSpan>,
        attachments: List<NoteAttachment>,
        content: NoteContent,
    ) {
        val idx = notes.indexOfFirst { it.id == id }.takeIf { it >= 0 } ?: return
        val now = System.currentTimeMillis()
        val finalContent = if (content.blocks.isEmpty()) NoteContent() else content
        notes[idx] = notes[idx].copy(
            title = title,
            description = description,
            deleted = deleted,
            updatedAt = now,
            folderId = folderId,
            descriptionSpans = spans,
            attachments = attachments,
            content = finalContent,
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
