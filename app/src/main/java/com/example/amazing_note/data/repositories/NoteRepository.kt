package com.example.amazing_note.data.repositories

import androidx.lifecycle.LiveData
import com.example.amazing_note.data.NoteDao
import com.example.amazing_note.data.models.Note

class NoteRepository(private val noteDao: NoteDao): INoteRepository {
    override fun listNotes(): LiveData<List<Note>> {
        return noteDao.getAllNotes()
    }

    override suspend fun insertNote(note: Note) {
        noteDao.insertNote(note)
    }

    override suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }

    override suspend fun deleteNote(note: Note) {

    }


}