package com.edufelip.shared.vm

import com.edufelip.shared.domain.validation.NoteActionResult
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.NoteAttachment
import com.edufelip.shared.domain.model.NoteBlock
import com.edufelip.shared.domain.model.NoteTextSpan
import kotlinx.coroutines.flow.Flow

interface NoteUiViewModel {
    val notes: Flow<List<Note>>
    val trash: Flow<List<Note>>
    val folders: Flow<List<Folder>>
    val notesWithoutFolder: Flow<List<Note>>

    fun notesByFolder(folderId: Long): Flow<List<Note>>

    suspend fun insert(
        title: String,
        description: String,
        spans: List<NoteTextSpan>,
        attachments: List<NoteAttachment>,
        folderId: Long?,
        blocks: List<NoteBlock> = emptyList(),
    ): NoteActionResult
    suspend fun update(
        id: Int,
        title: String,
        description: String,
        deleted: Boolean,
        spans: List<NoteTextSpan>,
        attachments: List<NoteAttachment>,
        folderId: Long?,
        blocks: List<NoteBlock> = emptyList(),
    ): NoteActionResult
    suspend fun setDeleted(id: Int, deleted: Boolean)
    suspend fun delete(id: Int)
    suspend fun assignToFolder(id: Int, folderId: Long?)
    suspend fun createFolder(name: String): Long
    suspend fun renameFolder(id: Long, name: String)
    suspend fun deleteFolder(id: Long)
}
