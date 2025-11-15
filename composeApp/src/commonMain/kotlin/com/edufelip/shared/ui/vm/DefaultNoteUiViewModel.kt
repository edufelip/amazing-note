package com.edufelip.shared.ui.vm

import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.domain.model.NoteAttachment
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.NoteTextSpan
import com.edufelip.shared.domain.usecase.NoteUseCases
import com.edufelip.shared.domain.validation.NoteActionResult
import com.edufelip.shared.domain.validation.NoteValidationError
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

class DefaultNoteUiViewModel(
    private val useCases: NoteUseCases,
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
) : SharedViewModel(dispatcher),
    NoteUiViewModel {
    private val _state = MutableStateFlow(NotesState())
    override val state: StateFlow<NotesState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<NotesEvent>(replay = 0, extraBufferCapacity = 1)
    override val events: SharedFlow<NotesEvent> = _events.asSharedFlow()

    init {
        useCases.observeNotes()
            .onEach { notes ->
                _state.update { it.copy(notes = notes) }
            }
            .collectInScope()

        useCases.observeTrash()
            .onEach { trash ->
                _state.update { it.copy(trash = trash) }
            }
            .collectInScope()

        useCases.observeFolders()
            .onEach { folders ->
                _state.update { it.copy(folders = folders) }
            }
            .collectInScope()

        useCases.observeNotesWithoutFolder()
            .onEach { unassigned ->
                _state.update { it.copy(notesWithoutFolder = unassigned) }
            }
            .collectInScope()
    }

    override fun notesByFolder(folderId: Long) = useCases.observeNotesByFolder(folderId)

    override fun insert(
        title: String,
        description: String,
        spans: List<NoteTextSpan>,
        attachments: List<NoteAttachment>,
        folderId: Long?,
        content: NoteContent,
        stableId: String?,
        navigateBack: Boolean,
        cleanupAttachments: Boolean,
    ) = launchValidatedAction(
        navigateBack = navigateBack,
        cleanupAttachments = cleanupAttachments,
        action = {
            useCases.insertNote(
                title = title,
                description = description,
                folderId = folderId,
                spans = spans,
                attachments = attachments,
                content = content,
                stableId = stableId,
            )
        },
    )

    override fun update(
        id: Int,
        title: String,
        description: String,
        deleted: Boolean,
        spans: List<NoteTextSpan>,
        attachments: List<NoteAttachment>,
        folderId: Long?,
        content: NoteContent,
        navigateBack: Boolean,
        cleanupAttachments: Boolean,
    ) = launchValidatedAction(
        navigateBack = navigateBack,
        cleanupAttachments = cleanupAttachments,
        action = {
            useCases.updateNote(
                id = id,
                title = title,
                description = description,
                deleted = deleted,
                folderId = folderId,
                spans = spans,
                attachments = attachments,
                content = content,
            )
        },
    )

    override fun setDeleted(id: Int, deleted: Boolean, syncAfter: Boolean) = launchAction(
        errorMessage = "Failed to update note",
        syncAfter = syncAfter,
    ) {
        useCases.setDeleted(id, deleted)
    }

    override fun delete(id: Int, syncAfter: Boolean) = launchAction(
        errorMessage = "Failed to delete note",
        syncAfter = syncAfter,
    ) {
        useCases.deleteNote(id)
    }

    override fun assignToFolder(id: Int, folderId: Long?, syncAfter: Boolean) = launchAction(
        errorMessage = "Failed to move note",
        syncAfter = syncAfter,
    ) {
        useCases.assignNoteToFolder(id, folderId)
    }

    override fun createFolder(name: String, syncAfter: Boolean) = launchAction(
        errorMessage = "Failed to create folder",
        syncAfter = syncAfter,
    ) {
        useCases.createFolder(name)
    }

    override fun renameFolder(id: Long, name: String, syncAfter: Boolean) = launchAction(
        errorMessage = "Failed to rename folder",
        syncAfter = syncAfter,
    ) {
        useCases.renameFolder(id, name)
    }

    override fun deleteFolder(id: Long, syncAfter: Boolean) = launchAction(
        errorMessage = "Failed to delete folder",
        syncAfter = syncAfter,
    ) {
        useCases.removeFolder(id)
    }

    override fun syncFromRemote(syncManager: NotesSyncManager) {
        launchInScope {
            runCatching { syncManager.syncNow() }
        }
    }

    private fun launchValidatedAction(
        navigateBack: Boolean,
        cleanupAttachments: Boolean,
        action: suspend () -> NoteActionResult,
    ) {
        launchInScope {
            runCatching { action() }
                .fold(
                    onSuccess = { result ->
                        handleNoteActionResult(result, navigateBack, cleanupAttachments)
                    },
                    onFailure = { throwable ->
                        emitError(throwable, "Failed to save note")
                    },
                )
        }
    }

    private suspend fun handleNoteActionResult(
        result: NoteActionResult,
        navigateBack: Boolean,
        cleanupAttachments: Boolean,
    ) {
        when (result) {
            NoteActionResult.Success -> emitNoteSaved(navigateBack, cleanupAttachments)
            is NoteActionResult.Invalid -> emitValidationErrors(result.errors)
        }
    }

    private fun launchAction(
        errorMessage: String,
        syncAfter: Boolean = false,
        block: suspend () -> Unit,
    ) {
        launchInScope {
            runCatching { block() }
                .fold(
                    onSuccess = {
                        emitSyncIfNeeded(syncAfter)
                    },
                    onFailure = { throwable ->
                        emitError(throwable, errorMessage)
                    },
                )
        }
    }

    private suspend fun emitNoteSaved(navigateBack: Boolean, cleanupAttachments: Boolean) {
        _events.emit(NotesEvent.NoteSaved(navigateBack, cleanupAttachments))
    }

    private suspend fun emitValidationErrors(errors: List<NoteValidationError>) {
        _events.emit(NotesEvent.ValidationFailed(errors))
    }

    private suspend fun emitSyncIfNeeded(syncAfter: Boolean) {
        if (syncAfter) {
            _events.emit(NotesEvent.SyncRequested)
        }
    }

    private suspend fun emitError(throwable: Throwable, fallbackMessage: String) {
        if (throwable is CancellationException) throw throwable
        _events.emit(NotesEvent.ShowMessage(throwable.message ?: fallbackMessage))
    }
}
