package com.example.amazing_note.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.amazing_note.data.models.Note
import com.example.amazing_note.data.models.Priority

class FakeNoteRepository : INoteRepository {
    private val notes = mutableListOf<Note>()
    private val deletedNotes = mutableListOf<Note>()
    private val observableNotes = MutableLiveData<List<Note>>(notes)
    private val observableDeletedNotes = MutableLiveData<List<Note>>(deletedNotes)

    private var shouldReturnNetworkError = false

    fun setShouldReturnNetworkError(value: Boolean) {
        shouldReturnNetworkError = value
    }

    private fun refreshLiveData() {
        observableNotes.postValue(notes)
        observableDeletedNotes.postValue(deletedNotes)
    }

    override fun listNotes(): LiveData<List<Note>> {
        return observableNotes
    }

    override fun listDeletedNotes(): LiveData<List<Note>> {
        return observableDeletedNotes
    }

    override fun sortByPriorityAsc(): LiveData<List<Note>> {
        val sorted = sortNoteList(notes)
        return MutableLiveData(sorted)
    }

    override fun sortByPriorityDes(): LiveData<List<Note>> {
        val sorted = sortNoteList(notes)
        sorted.reverse()
        return MutableLiveData(sorted)
    }

    override suspend fun insertNote(note: Note) {
        notes.add(note)
        refreshLiveData()
    }

    override suspend fun updateNote(note: Note) {
        val foundNote = notes.find { it.id == note.id }
        notes[notes.indexOf(foundNote)] = note
        refreshLiveData()
    }

    override suspend fun deleteNote(note: Note) {
        notes.removeAt(notes.indexOf(note))
    }

    override fun searchNote(searchQuery: String): LiveData<List<Note>> {
        val foundNotes = notes.filter { it.title.contains(searchQuery, true) }
        return MutableLiveData(foundNotes)
    }

    private fun sortNoteList(noteList: MutableList<Note>): MutableList<Note> {
        val sorted = mutableListOf<Note>()
        for (i in 0..2) {
            for (note in noteList) {
                when (i) {
                    0 -> {
                        if (note.priority == Priority.LOW) sorted.add(note)
                    }
                    1 -> {
                        if (note.priority == Priority.MEDIUM) sorted.add(note)
                    }
                    2 -> {
                        if (note.priority == Priority.HIGH) sorted.add(note)
                    }
                }
            }
        }
        return sorted
    }
}