package com.edufelipe.amazing_note.ui.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.edufelipe.amazing_note.MainCoroutineRule
import com.edufelipe.amazing_note.data.models.Note
import com.edufelipe.amazing_note.data.models.Priority
import com.edufelipe.amazing_note.data.repositories.FakeNoteRepository
import com.edufelipe.amazing_note.getOrAwaitValueTest
import com.edufelipe.amazing_note.others.Status
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class TrashViewModelTest {
    private lateinit var viewModel: TrashViewModel

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        viewModel = TrashViewModel(FakeNoteRepository())
    }

    @Test
    fun `Should delete a note permanently`() {
        val note = Note(0, "random title", Priority.HIGH, "random description", false)
        viewModel.permaDeleteNote(note)

        val value = viewModel.deleteNoteStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }

    @Test
    fun `Should properly recover a note`() {
        val note = Note(0, "random title", Priority.HIGH, "random description", false)
        viewModel.recoverNote(note)

        val value = viewModel.recoverNoteStatus.getOrAwaitValueTest()
        assertThat(value.getContentIfNotHandled()?.status).isEqualTo(Status.SUCCESS)
    }
}