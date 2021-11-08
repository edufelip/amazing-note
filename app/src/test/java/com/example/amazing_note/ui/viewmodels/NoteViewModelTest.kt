package com.example.amazing_note.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.amazing_note.MainCoroutineRule
import com.example.amazing_note.data.models.Priority
import com.example.amazing_note.data.repositories.FakeNoteRepository
import com.example.amazing_note.getOrAwaitValueTest
import com.example.amazing_note.others.Constants
import com.example.amazing_note.others.Status
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
}