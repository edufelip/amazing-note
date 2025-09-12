package com.edufelip.shared.domain.usecase

import com.edufelip.shared.domain.repository.NoteRepository
import com.edufelip.shared.domain.validation.NoteActionResult
import com.edufelip.shared.domain.validation.NoteValidationRules
import com.edufelip.shared.domain.validation.validateNoteInput
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import kotlinx.coroutines.flow.Flow

class ObserveNotes(private val repository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> = repository.notes()
}

class ObserveTrash(private val repository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> = repository.trash()
}

class InsertNote(
    private val repository: NoteRepository,
    private val rules: NoteValidationRules,
) {
    suspend operator fun invoke(title: String, priority: Priority, description: String): NoteActionResult {
        val errors = validateNoteInput(title, description, rules)
        if (errors.isNotEmpty()) return NoteActionResult.Invalid(errors)
        repository.insert(title, priority, description)
        return NoteActionResult.Success
    }
}

class UpdateNote(
    private val repository: NoteRepository,
    private val rules: NoteValidationRules,
) {
    suspend operator fun invoke(id: Int, title: String, priority: Priority, description: String, deleted: Boolean): NoteActionResult {
        val errors = validateNoteInput(title, description, rules)
        if (errors.isNotEmpty()) return NoteActionResult.Invalid(errors)
        repository.update(id, title, priority, description, deleted)
        return NoteActionResult.Success
    }
}

class SetDeleted(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Int, deleted: Boolean) = repository.setDeleted(id, deleted)
}

class DeleteNote(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Int) = repository.delete(id)
}

data class NoteUseCases(
    val observeNotes: ObserveNotes,
    val observeTrash: ObserveTrash,
    val insertNote: InsertNote,
    val updateNote: UpdateNote,
    val setDeleted: SetDeleted,
    val deleteNote: DeleteNote,
)

fun buildNoteUseCases(
    repository: NoteRepository,
    rules: NoteValidationRules = NoteValidationRules(),
): NoteUseCases = NoteUseCases(
    observeNotes = ObserveNotes(repository),
    observeTrash = ObserveTrash(repository),
    insertNote = InsertNote(repository, rules),
    updateNote = UpdateNote(repository, rules),
    setDeleted = SetDeleted(repository),
    deleteNote = DeleteNote(repository),
)
