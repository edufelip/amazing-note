package com.edufelip.amazing_note.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.edufelip.amazing_note.MainCoroutineRule
import com.edufelip.amazing_note.domain.FakeDomainRepository
import com.edufelip.amazing_note.others.Constants
import com.edufelip.shared.domain.usecase.buildNoteUseCases
import com.edufelip.shared.domain.validation.NoteActionResult
import com.edufelip.shared.domain.validation.NoteValidationRules
import com.edufelip.shared.model.Priority
import com.edufelip.shared.presentation.DefaultNoteUiViewModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DefaultNoteUiViewModelTest {
    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var vm: DefaultNoteUiViewModel

    @Before
    fun setup() {
        val repo = FakeDomainRepository()
        val useCases = buildNoteUseCases(
            repo,
            NoteValidationRules(Constants.MAX_TITLE_LENGTH, Constants.MAX_DESCRIPTION_LENGTH),
        )
        vm = DefaultNoteUiViewModel(useCases)
    }

    @Test
    fun insert_invalid_emptyTitle() = runTest {
        val result = vm.insert("", Priority.HIGH, "desc")
        assertThat(result).isInstanceOf(NoteActionResult.Invalid::class.java)
    }

    @Test
    fun insert_success_and_observeNotes() = runTest {
        val result = vm.insert("Title", Priority.MEDIUM, "Desc")
        assertThat(result).isInstanceOf(NoteActionResult.Success::class.java)
        val notes = vm.notes.first()
        assertThat(notes).hasSize(1)
        assertThat(notes.first().title).isEqualTo("Title")
    }

    @Test
    fun update_invalid_tooLongTitle() = runTest {
        vm.insert("T", Priority.LOW, "D")
        val existing = vm.notes.first().first()
        val longTitle = "x".repeat(Constants.MAX_TITLE_LENGTH + 1)
        val result = vm.update(existing.id, longTitle, existing.priority, existing.description, false)
        assertThat(result).isInstanceOf(NoteActionResult.Invalid::class.java)
    }

    @Test
    fun setDeleted_and_restore() = runTest {
        vm.insert("To Delete", Priority.LOW, "D")
        val note = vm.notes.first().first()
        vm.setDeleted(note.id, true)
        val trash1 = vm.trash.first()
        assertThat(trash1.map { it.id }).contains(note.id)
        vm.setDeleted(note.id, false)
        val trash2 = vm.trash.first()
        assertThat(trash2.map { it.id }).doesNotContain(note.id)
    }
}
