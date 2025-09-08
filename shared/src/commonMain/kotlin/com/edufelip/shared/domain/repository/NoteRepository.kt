package com.edufelip.shared.domain.repository

import com.edufelip.shared.model.Note
import com.edufelip.shared.model.Priority
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun notes(): Flow<List<Note>>
    fun trash(): Flow<List<Note>>
    suspend fun insert(title: String, priority: Priority, description: String)
    suspend fun update(id: Int, title: String, priority: Priority, description: String, deleted: Boolean)
    suspend fun setDeleted(id: Int, deleted: Boolean)
    suspend fun delete(id: Int)
}

