package com.edufelip.aqua_note.data.repositories

import androidx.lifecycle.LiveData
import com.edufelip.aqua_note.data.models.Note

interface INoteRepository {
    fun listNotes(): LiveData<List<Note>>

    fun listDeletedNotes(): LiveData<List<Note>>

    fun sortByPriorityAsc(): LiveData<List<Note>>

    fun sortByPriorityDes(): LiveData<List<Note>>

    suspend fun insertNote(note: Note)

    suspend fun updateNote(note: Note)

    suspend fun deleteNote(note: Note)

    fun searchNote(searchQuery: String): LiveData<List<Note>>
}