package com.edufelip.shared.presentation

import com.edufelip.shared.domain.validation.NoteActionResult
import com.edufelip.shared.model.Folder
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import kotlinx.coroutines.flow.Flow

interface NoteUiViewModel {
    val notes: Flow<List<Note>>
    val trash: Flow<List<Note>>
    val folders: Flow<List<Folder>>
    val notesWithoutFolder: Flow<List<Note>>

    fun notesByFolder(folderId: Long): Flow<List<Note>>

    suspend fun insert(title: String, priority: Priority, description: String, folderId: Long?): NoteActionResult
    suspend fun update(id: Int, title: String, priority: Priority, description: String, deleted: Boolean, folderId: Long?): NoteActionResult
    suspend fun setDeleted(id: Int, deleted: Boolean)
    suspend fun delete(id: Int)
    suspend fun assignToFolder(id: Int, folderId: Long?)
    suspend fun createFolder(name: String): Long
    suspend fun renameFolder(id: Long, name: String)
    suspend fun deleteFolder(id: Long)
}
