package com.example.amazing_note.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.amazing_note.data.models.Note

@Dao
interface NoteDao {
    @Query("SELECT * FROM note_table ORDER BY id ASC")
    fun getAllNotes(): LiveData<List<Note>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM note_table WHERE title LIKE :searchQuery")
    fun searchNote(searchQuery: String): LiveData<List<Note>>
}