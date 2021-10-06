package com.example.amazing_note.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.amazing_note.data.db.NoteDatabase
import com.example.amazing_note.data.models.Note
import com.example.amazing_note.data.repositories.INoteRepository
import com.example.amazing_note.data.repositories.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrashViewModel(application: Application): AndroidViewModel(application) {
    private val noteDao = NoteDatabase.getDatabase(application).noteDao()
    private val repository: INoteRepository

    val deletedNoteList: LiveData<List<Note>>

    init {
        repository = NoteRepository(noteDao)
        deletedNoteList = repository.listDeletedNotes()
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(note)
        }
    }

    fun recoverNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note)
        }
    }
}