package com.edufelip.amazing_note.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.edufelip.amazing_note.data.mappers.toAndroid
import com.edufelip.amazing_note.data.mappers.toShared
import com.edufelip.amazing_note.data.models.Note
import com.edufelip.amazing_note.data.models.Priority
import com.edufelip.shared.data.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class KmpAndroidNoteRepository(
    private val sharedRepo: NoteRepository
): INoteRepository {
    override fun listNotes(): LiveData<List<Note>> =
        sharedRepo.notes().map { list -> list.map { it.toAndroid() } }.asLiveData(Dispatchers.Default)

    override fun listDeletedNotes(): LiveData<List<Note>> =
        sharedRepo.trash().map { list -> list.map { it.toAndroid() } }.asLiveData(Dispatchers.Default)

    override fun sortByPriorityAsc(): LiveData<List<Note>> =
        sharedRepo.notes().map { list -> list
            .sortedBy { it.priority.ordinal } // HIGH(0), MEDIUM(1), LOW(2)
            .map { it.toAndroid() }
        }.asLiveData(Dispatchers.Default)

    override fun sortByPriorityDes(): LiveData<List<Note>> =
        sharedRepo.notes().map { list -> list
            .sortedByDescending { it.priority.ordinal }
            .map { it.toAndroid() }
        }.asLiveData(Dispatchers.Default)

    override suspend fun insertNote(note: Note) {
        sharedRepo.insert(note.title, note.priority.toShared(), note.description)
    }

    override suspend fun updateNote(note: Note) {
        sharedRepo.update(note.id, note.title, note.priority.toShared(), note.description, note.deleted)
    }

    override suspend fun deleteNote(note: Note) {
        // Permanent delete
        sharedRepo.delete(note.id)
    }

    override fun searchNote(searchQuery: String): LiveData<List<Note>> =
        sharedRepo.notes().map { list ->
            val q = searchQuery.trim()
            if (q.isBlank()) list else list.filter {
                it.title.contains(q, ignoreCase = true) || it.description.contains(q, ignoreCase = true)
            }
        }.map { it.map { n -> n.toAndroid() } }
            .asLiveData(Dispatchers.Default)
}

