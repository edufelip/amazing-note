package com.example.amazing_note.data.repositories

import androidx.lifecycle.LiveData
import com.example.amazing_note.data.db.NoteDao
import com.example.amazing_note.data.models.Note
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
): INoteRepository {
    override fun listNotes(): LiveData<List<Note>> {
        return noteDao.getAllNotes()
    }

    override fun listDeletedNotes(): LiveData<List<Note>> {
        return noteDao.getDeletedNotes()
    }

    override suspend fun insertNote(note: Note) {
        noteDao.insertNote(note)
    }

    override suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }

    override suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    override fun searchNote(searchQuery: String): LiveData<List<Note>> {
        return noteDao.searchNote(searchQuery)
    }

    override fun sortByPriorityAsc(): LiveData<List<Note>> {
        return noteDao.sortByPriorityAsc()
    }

    override fun sortByPriorityDes(): LiveData<List<Note>> {
        return noteDao.sortByPriorityDes()
    }
}