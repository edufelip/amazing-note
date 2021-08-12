package com.example.amazing_note.data.repositories

import androidx.lifecycle.LiveData
import com.example.amazing_note.data.models.Note

interface INoteRepository {
    fun listNotes(): LiveData<List<Note>>

    suspend fun insertNote(note: Note)

    suspend fun updateNote(note: Note)

    suspend fun deleteNote(note: Note)

}