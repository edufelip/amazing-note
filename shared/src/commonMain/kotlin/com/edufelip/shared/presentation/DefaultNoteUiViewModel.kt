package com.edufelip.shared.presentation

import com.edufelip.shared.domain.usecase.NoteUseCases
import com.edufelip.shared.domain.validation.NoteActionResult
import com.edufelip.shared.model.Priority

class DefaultNoteUiViewModel(
    private val useCases: NoteUseCases,
) : NoteUiViewModel {
    override val notes = useCases.observeNotes()
    override val trash = useCases.observeTrash()
    override val folders = useCases.observeFolders()
    override val notesWithoutFolder = useCases.observeNotesWithoutFolder()

    override fun notesByFolder(folderId: Long) = useCases.observeNotesByFolder(folderId)

    override suspend fun insert(title: String, priority: Priority, description: String, folderId: Long?): NoteActionResult = useCases.insertNote(title, priority, description, folderId)

    override suspend fun update(
        id: Int,
        title: String,
        priority: Priority,
        description: String,
        deleted: Boolean,
        folderId: Long?,
    ): NoteActionResult = useCases.updateNote(id, title, priority, description, deleted, folderId)

    override suspend fun setDeleted(id: Int, deleted: Boolean) = useCases.setDeleted(id, deleted)

    override suspend fun delete(id: Int) = useCases.deleteNote(id)

    override suspend fun assignToFolder(id: Int, folderId: Long?) = useCases.assignNoteToFolder(id, folderId)

    override suspend fun createFolder(name: String): Long = useCases.createFolder(name)

    override suspend fun renameFolder(id: Long, name: String) = useCases.renameFolder(id, name)

    override suspend fun deleteFolder(id: Long) = useCases.removeFolder(id)
}
