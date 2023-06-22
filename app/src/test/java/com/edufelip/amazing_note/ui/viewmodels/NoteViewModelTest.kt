package com.edufelip.amazing_note.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.edufelip.amazing_note.MainCoroutineRule
import com.edufelip.amazing_note.data.models.Note
import com.edufelip.amazing_note.data.models.Priority
import com.edufelip.amazing_note.data.repositories.FakeNoteRepository
import com.edufelip.amazing_note.getOrAwaitValueTest
import com.edufelip.amazing_note.others.Constants
import com.edufelip.amazing_note.others.Status
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class NoteViewModelTest {
    private lateinit var viewModel: NoteViewModel
    private val fakeTitle = "Fake Title"
    private val fakeDescription = "Fake Description"
    private val fakePriority = Priority.HIGH

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        viewModel = NoteViewModel(FakeNoteRepository())
    }

    @Test
    fun `Should NOT insert note with empty title`() {
        viewModel.insertNote("", fakePriority, fakeDescription)

        val value = viewModel.insertNoteStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `Should NOT insert note with empty description`() {
        viewModel.insertNote(fakeTitle, fakePriority, "")

        val value = viewModel.insertNoteStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `Should NOT insert note with title too long`() {
        val longTitle = buildString {
            for(i in 1..Constants.MAX_TITLE_LENGTH + 1) {
                append(1)
            }
        }
        viewModel.insertNote(longTitle, fakePriority, fakeDescription)

        val value = viewModel.insertNoteStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `Should NOT insert note with description too long`() {
        val longDescription = buildString {
            for(i in 1..Constants.MAX_DESCRIPTION_LENGTH + 1) {
                append(1)
            }
        }
        viewModel.insertNote(fakeTitle, fakePriority, longDescription)

        val value = viewModel.insertNoteStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `Should insert note successfully`() {
        viewModel.insertNote(fakeTitle, fakePriority, fakeDescription)

        val value = viewModel.insertNoteStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `Should NOT update note with empty title`() {
        viewModel.updateNote(0 , "", Priority.HIGH, "random description", false)

        val value = viewModel.updateNoteStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `Should NOT update note with empty description`() {
        viewModel.updateNote(0 , "random title", Priority.HIGH, "", false)

        val value = viewModel.updateNoteStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `Should NOT update note with title too long`() {
        val longTitle = buildString {
            for(i in 1..Constants.MAX_TITLE_LENGTH + 1) {
                append(i)
            }
        }
        viewModel.updateNote(0, longTitle, Priority.HIGH, "random description", false)

        val value = viewModel.updateNoteStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `Should NOT update note with description too long`() {
        val longDescription = buildString {
            for(i in 1..Constants.MAX_DESCRIPTION_LENGTH + 1) {
                append(1)
            }
        }
        viewModel.updateNote(0, "random title", Priority.HIGH, longDescription, false)

        val value = viewModel.updateNoteStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.ERROR)
    }

    @Test
    fun `Should successfully update a note`() {
        viewModel.updateNote(0, "random title", Priority.HIGH, "random description", false)

        val value = viewModel.updateNoteStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `Should successfully delete a note`() {
        val note = Note(0, "random title", Priority.HIGH, "random description", false)
        viewModel.deleteNote(note, true)

        val value = viewModel.deleteNoteStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }
}