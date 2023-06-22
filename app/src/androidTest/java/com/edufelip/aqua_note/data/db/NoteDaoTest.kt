package com.edufelip.aqua_note.data.db

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.filters.SmallTest
import com.edufelip.aqua_note.data.models.Note
import com.edufelip.aqua_note.data.models.Priority
import com.edufelip.aqua_note.getOrAwaitValue
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
    private val defaultNote = Note(1, "random title", Priority.HIGH, "random description", false)

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
    fun tearDown() {
        database.close()
    }

    @Test
    fun getAllNotes() = runBlockingTest {
        val supNote = Note(0, "another note", Priority.MEDIUM, "another description", false)
        dao.insertNote(defaultNote)
        dao.insertNote(supNote)
        val allNotes = dao.getAllNotes().getOrAwaitValue()
        assertThat(allNotes).hasSize(2)
    }

    @Test
    fun getDeletedNotes() = runBlockingTest {
        val supNote = defaultNote
        supNote.deleted = true
        dao.insertNote(defaultNote)
        dao.updateNote(supNote)
        val allDeletedNotes = dao.getDeletedNotes().getOrAwaitValue()
        assertThat(allDeletedNotes).hasSize(1)
    }

    @Test
    fun insertNote() = runBlockingTest {
        dao.insertNote(defaultNote)
        val allNotes = dao.getAllNotes().getOrAwaitValue()
        assertThat(allNotes).contains(defaultNote)
    }

    @Test
    fun updateNote() = runBlockingTest {
        val supNote = defaultNote
        dao.insertNote(supNote)
        supNote.description = "another description"
        dao.updateNote(supNote)
        val allNotes = dao.getAllNotes().getOrAwaitValue()
        assertThat(allNotes).contains(supNote)
    }

    @Test
    fun deleteNote() = runBlockingTest {
        dao.insertNote(defaultNote)
        dao.deleteNote(defaultNote)
        val allNotes = dao.getAllNotes().getOrAwaitValue()
        assertThat(allNotes).isEmpty()
    }

    @Test
    fun searchNote() = runBlockingTest {
        dao.insertNote(defaultNote)
        val searchNote = dao.searchNote(defaultNote.title).getOrAwaitValue()
        assertThat(searchNote[0]).isEqualTo(defaultNote)
    }

    @Test
    fun sortPriorityAsc() = runBlockingTest {
        val supNote = Note(0, "another note", Priority.LOW, "another description", false)
        dao.insertNote(defaultNote)
        dao.insertNote(supNote)
        val notesAsc = dao.sortByPriorityAsc().getOrAwaitValue()
        assertThat(notesAsc[0].priority).isEqualTo(Priority.HIGH)
        assertThat(notesAsc[1].priority).isEqualTo(Priority.LOW)
    }

    @Test
    fun sortPriorityDes() = runBlockingTest {
        val supNote = Note(0, "another note", Priority.LOW, "another description", false)
        dao.insertNote(defaultNote)
        dao.insertNote(supNote)
        val notesAsc = dao.sortByPriorityDes().getOrAwaitValue()
        assertThat(notesAsc[0].priority).isEqualTo(Priority.LOW)
        assertThat(notesAsc[1].priority).isEqualTo(Priority.HIGH)
    }
}