package com.edufelip.shared.ui.vm

import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.NoteAttachment
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.NoteTextSpan
import com.edufelip.shared.domain.validation.NoteValidationError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

data class NotesState(
    val notes: List<Note> = emptyList(),
    val trash: List<Note> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val notesWithoutFolder: List<Note> = emptyList(),
)

sealed interface NotesEvent {
    data class NoteSaved(val navigateBack: Boolean, val cleanupAttachments: Boolean) : NotesEvent
    data class ValidationFailed(val errors: List<NoteValidationError>) : NotesEvent
    data class ShowMessage(val text: String) : NotesEvent
    data object SyncRequested : NotesEvent
}

interface NoteUiViewModel {
    val state: StateFlow<NotesState>
    val events: SharedFlow<NotesEvent>

    fun notesByFolder(folderId: Long): Flow<List<Note>>

    fun insert(
        title: String,
        description: String,
        spans: List<NoteTextSpan>,
        attachments: List<NoteAttachment>,
        folderId: Long?,
        content: NoteContent = NoteContent(),
        stableId: String? = null,
        navigateBack: Boolean = false,
        cleanupAttachments: Boolean = false,
    )
    fun update(
        id: Int,
        title: String,
        description: String,
        deleted: Boolean,
        spans: List<NoteTextSpan>,
        attachments: List<NoteAttachment>,
        folderId: Long?,
        content: NoteContent = NoteContent(),
        navigateBack: Boolean = false,
        cleanupAttachments: Boolean = false,
    )
    fun setDeleted(id: Int, deleted: Boolean, syncAfter: Boolean = false)
    fun delete(id: Int, syncAfter: Boolean = false)
    fun assignToFolder(id: Int, folderId: Long?, syncAfter: Boolean = false)
    fun createFolder(name: String, syncAfter: Boolean = false)
    fun renameFolder(id: Long, name: String, syncAfter: Boolean = false)
    fun deleteFolder(id: Long, syncAfter: Boolean = false)
}
