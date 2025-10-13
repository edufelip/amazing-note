package com.edufelip.shared.domain.usecase

import com.edufelip.shared.domain.repository.NoteRepository
import com.edufelip.shared.domain.validation.NoteActionResult
import com.edufelip.shared.domain.validation.NoteValidationRules
import com.edufelip.shared.domain.validation.validateNoteInput
import com.edufelip.shared.model.Folder
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.NoteAttachment
import com.edufelip.shared.model.NoteBlock
import com.edufelip.shared.model.NoteTextSpan
import com.edufelip.shared.model.ensureBlocks
import kotlinx.coroutines.flow.Flow

class ObserveNotes(private val repository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> = repository.notes()
}

class ObserveTrash(private val repository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> = repository.trash()
}

class ObserveNotesByFolder(private val repository: NoteRepository) {
    operator fun invoke(folderId: Long): Flow<List<Note>> = repository.notesByFolder(folderId)
}

class ObserveNotesWithoutFolder(private val repository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> = repository.notesWithoutFolder()
}

class ObserveFolders(private val repository: NoteRepository) {
    operator fun invoke(): Flow<List<Folder>> = repository.folders()
}

class InsertNote(
    private val repository: NoteRepository,
    private val rules: NoteValidationRules,
) {
    suspend operator fun invoke(
        title: String,
        description: String,
        folderId: Long?,
        spans: List<NoteTextSpan>,
        attachments: List<NoteAttachment>,
        blocks: List<NoteBlock> = emptyList(),
    ): NoteActionResult {
        val errors = validateNoteInput(title, description, rules)
        if (errors.isNotEmpty()) return NoteActionResult.Invalid(errors)
        val finalBlocks = ensureBlocks(description, spans, attachments, blocks)
        repository.insert(title, description, folderId, spans, attachments, finalBlocks)
        return NoteActionResult.Success
    }
}

class UpdateNote(
    private val repository: NoteRepository,
    private val rules: NoteValidationRules,
) {
    suspend operator fun invoke(
        id: Int,
        title: String,
        description: String,
        deleted: Boolean,
        folderId: Long?,
        spans: List<NoteTextSpan>,
        attachments: List<NoteAttachment>,
        blocks: List<NoteBlock> = emptyList(),
    ): NoteActionResult {
        val errors = validateNoteInput(title, description, rules)
        if (errors.isNotEmpty()) return NoteActionResult.Invalid(errors)
        val finalBlocks = ensureBlocks(description, spans, attachments, blocks)
        repository.update(id, title, description, deleted, folderId, spans, attachments, finalBlocks)
        return NoteActionResult.Success
    }
}

class SetDeleted(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Int, deleted: Boolean) = repository.setDeleted(id, deleted)
}

class DeleteNote(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Int) = repository.delete(id)
}

class AssignNoteToFolder(private val repository: NoteRepository) {
    suspend operator fun invoke(noteId: Int, folderId: Long?) = repository.assignToFolder(noteId, folderId)
}

class CreateFolder(private val repository: NoteRepository) {
    suspend operator fun invoke(name: String): Long = repository.insertFolder(name)
}

class RenameFolder(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Long, name: String) = repository.renameFolder(id, name)
}

class RemoveFolder(private val repository: NoteRepository) {
    suspend operator fun invoke(id: Long) = repository.deleteFolder(id)
}

data class NoteUseCases(
    val observeNotes: ObserveNotes,
    val observeTrash: ObserveTrash,
    val observeNotesByFolder: ObserveNotesByFolder,
    val observeNotesWithoutFolder: ObserveNotesWithoutFolder,
    val observeFolders: ObserveFolders,
    val insertNote: InsertNote,
    val updateNote: UpdateNote,
    val setDeleted: SetDeleted,
    val deleteNote: DeleteNote,
    val assignNoteToFolder: AssignNoteToFolder,
    val createFolder: CreateFolder,
    val renameFolder: RenameFolder,
    val removeFolder: RemoveFolder,
)

fun buildNoteUseCases(
    repository: NoteRepository,
    rules: NoteValidationRules = NoteValidationRules(),
): NoteUseCases = NoteUseCases(
    observeNotes = ObserveNotes(repository),
    observeTrash = ObserveTrash(repository),
    observeNotesByFolder = ObserveNotesByFolder(repository),
    observeNotesWithoutFolder = ObserveNotesWithoutFolder(repository),
    observeFolders = ObserveFolders(repository),
    insertNote = InsertNote(repository, rules),
    updateNote = UpdateNote(repository, rules),
    setDeleted = SetDeleted(repository),
    deleteNote = DeleteNote(repository),
    assignNoteToFolder = AssignNoteToFolder(repository),
    createFolder = CreateFolder(repository),
    renameFolder = RenameFolder(repository),
    removeFolder = RemoveFolder(repository),
)
