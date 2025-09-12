package com.edufelip.shared.presentation

import com.edufelip.shared.domain.usecase.NoteUseCases
import com.edufelip.shared.domain.validation.NoteActionResult
import com.edufelip.shared.model.Priority

class DefaultNoteUiViewModel(
    private val useCases: NoteUseCases,
) : NoteUiViewModel {
    override val notes = useCases.observeNotes()
    override val trash = useCases.observeTrash()

    override suspend fun insert(title: String, priority: Priority, description: String): NoteActionResult = useCases.insertNote(title, priority, description)

    override suspend fun update(
        id: Int,
        title: String,
        priority: Priority,
        description: String,
        deleted: Boolean,
    ): NoteActionResult = useCases.updateNote(id, title, priority, description, deleted)

    override suspend fun setDeleted(id: Int, deleted: Boolean) = useCases.setDeleted(id, deleted)

    override suspend fun delete(id: Int) = useCases.deleteNote(id)
}
