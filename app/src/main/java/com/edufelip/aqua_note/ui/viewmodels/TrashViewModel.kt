package com.edufelip.aqua_note.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.edufelip.aqua_note.data.models.Note
import com.edufelip.aqua_note.data.repositories.INoteRepository
import com.edufelip.aqua_note.others.Event
import com.edufelip.aqua_note.others.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val repository: INoteRepository
): ViewModel() {
    val deletedNoteList: LiveData<List<Note>> = repository.listDeletedNotes()

    private val _deleteNoteStatus = MutableLiveData<Event<Resource<Note>>>()
    val deleteNoteStatus: LiveData<Event<Resource<Note>>> = _deleteNoteStatus
    private val _recoverNoteStatus = MutableLiveData<Event<Resource<Note>>>()
    val recoverNoteStatus: LiveData<Event<Resource<Note>>> = _recoverNoteStatus

    fun permaDeleteNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(note)
        }
        _deleteNoteStatus.postValue(Event(Resource.success(note)))
    }

    fun recoverNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note)
        }
        _recoverNoteStatus.postValue(Event(Resource.success(note)))
    }
}