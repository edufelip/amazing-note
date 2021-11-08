package com.example.amazing_note.data.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.example.amazing_note.data.models.Note
import com.example.amazing_note.data.models.Priority
import com.example.amazing_note.getOrAwaitValue
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
@SmallTest
@HiltAndroidTest
class NoteDaoTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Inject
    @Named("test_db")
    lateinit var database: NoteDatabase
    private lateinit var dao: NoteDao

    @Before
    fun setup() {
        hiltRule.inject()
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