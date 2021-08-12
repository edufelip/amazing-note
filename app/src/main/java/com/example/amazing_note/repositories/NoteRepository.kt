package com.example.amazing_note.repositories

import androidx.lifecycle.LiveData
import com.example.amazing_note.data.NoteDao
import com.example.amazing_note.models.Note

class NoteRepository(private val noteDao: NoteDao): INoteRepository {
    override fun listNotes(): LiveData<List<Note>> {
        return noteDao.getAllNotes()
    }

    override suspend fun insertNote(note: Note) {
        noteDao.insertNote(note)
    }

    override suspend fun deleteNote(note: Note) {
        
    }


}