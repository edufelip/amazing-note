package com.example.amazing_note.data.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.amazing_note.data.models.Note
import com.example.amazing_note.data.models.Priority
import com.example.amazing_note.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class NoteDaoTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: NoteDatabase
    private lateinit var dao: NoteDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NoteDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.noteDao()
    }

    @After
    fun finalize() {
        database.close()
    }

    @Test
    fun insertNote()  = runBlockingTest {
        val note = Note(1, "test title", Priority.MEDIUM, "test description", false)
        dao.insertNote(note)
        val allNotes = dao.getAllNotes().getOrAwaitValue()
        assertThat(allNotes).contains(note)
    }

    @Test
    fun deleteNote() = runBlockingTest {
        val note = Note(1, "test title", Priority.MEDIUM, "test description", false)
        dao.insertNote(note)
        dao.deleteNote(note)
        val allNotes = dao.getAllNotes().getOrAwaitValue()
        assertThat(allNotes).doesNotContain(note)
    }

    @Test
    fun updateNote() = runBlockingTest {
        val note = Note(1, "test title", Priority.MEDIUM, "test description", false)
        dao.insertNote(note)
        note.title = "new title"
        dao.updateNote(note)
        val allNotes = dao.getAllNotes().getOrAwaitValue()
        assertThat(allNotes).contains(note)
    }
}