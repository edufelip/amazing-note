package com.edufelip.shared.presentation

import com.edufelip.shared.domain.validation.NoteActionResult
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import kotlinx.coroutines.flow.Flow

interface NoteUiViewModel {
    val notes: Flow<List<Note>>
    val trash: Flow<List<Note>>

    suspend fun insert(title: String, priority: Priority, description: String): NoteActionResult
    suspend fun update(id: Int, title: String, priority: Priority, description: String, deleted: Boolean): NoteActionResult
    suspend fun setDeleted(id: Int, deleted: Boolean)
    suspend fun delete(id: Int)
}

