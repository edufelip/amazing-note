package com.edufelip.shared.vm

import com.edufelip.shared.domain.usecase.NoteUseCases
import com.edufelip.shared.domain.validation.NoteActionResult
import com.edufelip.shared.domain.model.NoteAttachment
import com.edufelip.shared.domain.model.NoteBlock
import com.edufelip.shared.domain.model.NoteTextSpan

class DefaultNoteUiViewModel(
    private val useCases: NoteUseCases,
) : NoteUiViewModel {
    override val notes = useCases.observeNotes()
    override val trash = useCases.observeTrash()
    override val folders = useCases.observeFolders()
    override val notesWithoutFolder = useCases.observeNotesWithoutFolder()

    override fun notesByFolder(folderId: Long) = useCases.observeNotesByFolder(folderId)

    override suspend fun insert(
        title: String,
        description: String,
        spans: List<NoteTextSpan>,
        attachments: List<NoteAttachment>,
        folderId: Long?,
        blocks: List<NoteBlock>,
    ): NoteActionResult = useCases.insertNote(title, description, folderId, spans, attachments, blocks)

    override suspend fun update(
        id: Int,
        title: String,
        description: String,
        deleted: Boolean,
        spans: List<NoteTextSpan>,
        attachments: List<NoteAttachment>,
        folderId: Long?,
        blocks: List<NoteBlock>,
    ): NoteActionResult = useCases.updateNote(id, title, description, deleted, folderId, spans, attachments, blocks)

    override suspend fun setDeleted(id: Int, deleted: Boolean) = useCases.setDeleted(id, deleted)

    override suspend fun delete(id: Int) = useCases.deleteNote(id)

    override suspend fun assignToFolder(id: Int, folderId: Long?) = useCases.assignNoteToFolder(id, folderId)

    override suspend fun createFolder(name: String): Long = useCases.createFolder(name)

    override suspend fun renameFolder(id: Long, name: String) = useCases.renameFolder(id, name)

    override suspend fun deleteFolder(id: Long) = useCases.removeFolder(id)
}
