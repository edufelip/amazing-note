package com.edufelip.aqua_note.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.edufelip.aqua_note.data.models.Note

@Dao
interface NoteDao {
    @Query("SELECT * FROM note_table WHERE deleted = 0 ORDER BY id ASC")
    fun getAllNotes(): LiveData<List<Note>>

    @Query("SELECT * FROM note_table WHERE deleted = 1 ORDER BY id ASC")
    fun getDeletedNotes(): LiveData<List<Note>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("SELECT * FROM note_table WHERE title LIKE :searchQuery AND deleted = 0")
    fun searchNote(searchQuery: String): LiveData<List<Note>>

    @Query("SELECT * FROM note_table WHERE deleted = 0 ORDER BY CASE WHEN priority LIKE 'H%' THEN 1 WHEN priority LIKE 'M%' THEN 2 WHEN priority LIKE 'L%' THEN 3 END")
    fun sortByPriorityAsc(): LiveData<List<Note>>

    @Query("SELECT * FROM note_table WHERE deleted = 0 ORDER BY CASE WHEN priority LIKE 'H%' THEN 3 WHEN priority LIKE 'M%' THEN 2 WHEN priority LIKE 'L%' THEN 1 END")
    fun sortByPriorityDes(): LiveData<List<Note>>
}