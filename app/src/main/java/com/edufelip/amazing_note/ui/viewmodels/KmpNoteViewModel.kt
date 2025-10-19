package com.edufelip.amazing_note.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.edufelip.shared.domain.usecase.NoteUseCases
import com.edufelip.shared.domain.validation.NoteActionResult
import com.edufelip.shared.model.NoteAttachment
import com.edufelip.shared.model.NoteBlock
import com.edufelip.shared.model.NoteTextSpan
import com.edufelip.shared.presentation.DefaultNoteUiViewModel
import com.edufelip.shared.presentation.NoteUiViewModel
import com.edufelip.shared.ui.settings.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class KmpNoteViewModel @Inject constructor(
    useCases: NoteUseCases,
    private val appPreferences: AppPreferences,
) : ViewModel(),
    NoteUiViewModel {
    private val delegate = DefaultNoteUiViewModel(useCases)

    override val notes = delegate.notes
    override val trash = delegate.trash
    override val folders = delegate.folders
    override val notesWithoutFolder = delegate.notesWithoutFolder

    override fun notesByFolder(folderId: Long) = delegate.notesByFolder(folderId)

    override suspend fun insert(
        title: String,
        description: String,
        spans: List<NoteTextSpan>,
        attachments: List<NoteAttachment>,
        folderId: Long?,
        blocks: List<NoteBlock>,
    ): NoteActionResult = delegate.insert(title, description, spans, attachments, folderId, blocks)

    override suspend fun update(
        id: Int,
        title: String,
        description: String,
        deleted: Boolean,
        spans: List<NoteTextSpan>,
        attachments: List<NoteAttachment>,
        folderId: Long?,
        blocks: List<NoteBlock>,
    ): NoteActionResult = delegate.update(id, title, description, deleted, spans, attachments, folderId, blocks)

    override suspend fun setDeleted(id: Int, deleted: Boolean) = delegate.setDeleted(id, deleted)

    override suspend fun delete(id: Int) = delegate.delete(id)

    override suspend fun assignToFolder(id: Int, folderId: Long?) = delegate.assignToFolder(id, folderId)

    override suspend fun createFolder(name: String): Long = delegate.createFolder(name)

    override suspend fun renameFolder(id: Long, name: String) = delegate.renameFolder(id, name)

    override suspend fun deleteFolder(id: Long) = delegate.deleteFolder(id)

    fun isDarkThemePref(): Boolean = appPreferences.isDarkTheme()
    fun setDarkThemePref(enabled: Boolean) = appPreferences.setDarkTheme(enabled)

    fun isDateModeUpdatedPref(): Boolean = appPreferences.isDateModeUpdated()
    fun setDateModeUpdatedPref(updated: Boolean) = appPreferences.setDateModeUpdated(updated)
}
