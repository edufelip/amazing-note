package com.edufelip.amazing_note.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.edufelip.shared.domain.usecase.NoteUseCases
import com.edufelip.shared.domain.validation.NoteActionResult
import com.edufelip.shared.model.Priority
import com.edufelip.shared.presentation.DefaultNoteUiViewModel
import com.edufelip.shared.presentation.NoteUiViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class KmpNoteViewModel @Inject constructor(
    useCases: NoteUseCases
) : ViewModel(), NoteUiViewModel {
    private val delegate = DefaultNoteUiViewModel(useCases)

    override val notes = delegate.notes
    override val trash = delegate.trash

    override suspend fun insert(title: String, priority: Priority, description: String): NoteActionResult =
        delegate.insert(title, priority, description)

    override suspend fun update(
        id: Int,
        title: String,
        priority: Priority,
        description: String,
        deleted: Boolean
    ): NoteActionResult = delegate.update(id, title, priority, description, deleted)

    override suspend fun setDeleted(id: Int, deleted: Boolean) = delegate.setDeleted(id, deleted)

    override suspend fun delete(id: Int) = delegate.delete(id)
}
