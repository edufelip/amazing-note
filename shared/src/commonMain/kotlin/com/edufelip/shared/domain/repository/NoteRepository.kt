package com.edufelip.shared.domain.repository

import com.edufelip.shared.model.Folder
import com.edufelip.shared.model.Note
import com.edufelip.shared.model.NoteAttachment
import com.edufelip.shared.model.NoteBlock
import com.edufelip.shared.model.NoteTextSpan
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun notes(): Flow<List<Note>>
    fun trash(): Flow<List<Note>>
    fun notesByFolder(folderId: Long): Flow<List<Note>>
    fun notesWithoutFolder(): Flow<List<Note>>
    fun folders(): Flow<List<Folder>>
    suspend fun insert(
        title: String,
        description: String,
        folderId: Long? = null,
        spans: List<NoteTextSpan> = emptyList(),
        attachments: List<NoteAttachment> = emptyList(),
        blocks: List<NoteBlock> = emptyList(),
    )
    suspend fun update(
        id: Int,
        title: String,
        description: String,
        deleted: Boolean,
        folderId: Long? = null,
        spans: List<NoteTextSpan> = emptyList(),
        attachments: List<NoteAttachment> = emptyList(),
        blocks: List<NoteBlock> = emptyList(),
    )
    suspend fun setDeleted(id: Int, deleted: Boolean)
    suspend fun delete(id: Int)
    suspend fun assignToFolder(id: Int, folderId: Long?)
    suspend fun insertFolder(name: String): Long
    suspend fun renameFolder(id: Long, name: String)
    suspend fun deleteFolder(id: Long)
}
