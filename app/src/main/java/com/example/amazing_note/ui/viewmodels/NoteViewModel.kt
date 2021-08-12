package com.example.amazing_note.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.amazing_note.data.NoteDatabase
import com.example.amazing_note.models.Note
import com.example.amazing_note.repositories.INoteRepository
import com.example.amazing_note.repositories.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NoteViewModel(application: Application): AndroidViewModel(application) {
    private val noteDao = NoteDatabase.getDatabase(application).noteDao()
    private val repository: INoteRepository

    private val noteList: LiveData<List<Note>>

    init {
        repository = NoteRepository(noteDao)
        noteList = repository.listNotes()
    }

    fun insertNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(note)
        }
    }
}