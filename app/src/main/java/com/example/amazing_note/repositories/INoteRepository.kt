package com.example.amazing_note.repositories

import androidx.lifecycle.LiveData
import com.example.amazing_note.models.Note

interface INoteRepository {
    fun listNotes(): LiveData<List<Note>>

    suspend fun insertNote(note: Note)

    suspend fun deleteNote(note: Note)

}