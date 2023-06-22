package com.edufelip.aqua_note.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edufelip.aqua_note.data.models.Note
import com.edufelip.aqua_note.data.models.Priority
import com.edufelip.aqua_note.data.repositories.INoteRepository
import com.edufelip.aqua_note.others.checkEmptyInput
import com.edufelip.aqua_note.others.checkTooLong
import com.edufelip.aqua_note.others.Constants
import com.edufelip.aqua_note.others.Event
import com.edufelip.aqua_note.others.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val repository: INoteRepository
): ViewModel() {
    val noteList: LiveData<List<Note>> = repository.listNotes()
    val noteListAsc: LiveData<List<Note>> = repository.sortByPriorityAsc()
    val noteListDes: LiveData<List<Note>> = repository.sortByPriorityDes()

    private val _insertNoteStatus = MutableLiveData<Event<Resource<Note>>>()
    val insertNoteStatus: LiveData<Event<Resource<Note>>> = _insertNoteStatus
    private val _updateNoteStatus = MutableLiveData<Event<Resource<Note>>>()
    val updateNoteStatus: LiveData<Event<Resource<Note>>> = _updateNoteStatus
    private val _deleteNoteStatus = MutableLiveData<Event<Resource<Note>>>()
    val deleteNoteStatus: LiveData<Event<Resource<Note>>> = _deleteNoteStatus

    fun insertNote(title: String, priority: Priority, description: String) {
        if(checkErrors(title, description, _insertNoteStatus)) return

        val note = Note(0, title, priority, description, false)
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(note)
        }
        _insertNoteStatus.postValue(Event(Resource.success(note)))
    }

    fun updateNote(id: Int, title: String, priority: Priority, description: String, deleted: Boolean) {
        if(checkErrors(title, description, _updateNoteStatus)) return

        val updatedNote = Note(id, title, priority, description, deleted)
        viewModelScope.launch(Dispatchers.IO){
            repository.updateNote(updatedNote)
        }
        _updateNoteStatus.postValue(Event(Resource.success(updatedNote)))
    }

    fun deleteNote(note: Note, deleted: Boolean) {
        note.deleted = deleted
        viewModelScope.launch(Dispatchers.IO){
            repository.updateNote(note)
        }
        _deleteNoteStatus.postValue(Event(Resource.success(note)))
    }

    fun searchNote(searchQuery: String): LiveData<List<Note>> {
        return repository.searchNote(searchQuery)
    }

    private fun checkErrors(title: String, description: String, status: MutableLiveData<Event<Resource<Note>>>): Boolean {
        if(checkEmptyInput(title, description)) {
            status.postValue(Event(Resource.error("empty_field", null)))
            return true
        }
        if(checkTooLong(title, Constants.MAX_TITLE_LENGTH)) {
            status.postValue(Event(Resource.error("title_too_long", null)))
            return true
        }
        if(checkTooLong(description, Constants.MAX_DESCRIPTION_LENGTH)) {
            status.postValue(Event(Resource.error("description_too_long", null)))
            return true
        }
        return false
    }
}