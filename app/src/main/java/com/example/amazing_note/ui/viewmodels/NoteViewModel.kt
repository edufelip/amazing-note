package com.example.amazing_note.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.amazing_note.data.models.Note
import com.example.amazing_note.data.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: NoteRepository
): ViewModel() {
    val noteList: LiveData<List<Note>> = repository.listNotes()
    val noteListAsc: LiveData<List<Note>> = repository.sortByPriorityAsc()
    val noteListDes: LiveData<List<Note>> = repository.sortByPriorityDes()

    fun insertNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(note)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO){
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(note)
        }
    }

    fun searchNote(searchQuery: String): LiveData<List<Note>> {
        return repository.searchNote(searchQuery)
    }
}