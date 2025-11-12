package com.edufelip.amazing_note.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.edufelip.amazing_note.MainCoroutineRule
import com.edufelip.amazing_note.domain.FakeDomainRepository
import com.edufelip.amazing_note.others.Constants
import com.edufelip.shared.domain.model.NoteAttachment
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.NoteTextSpan
import com.edufelip.shared.domain.usecase.buildNoteUseCases
import com.edufelip.shared.domain.validation.NoteValidationRules
import com.edufelip.shared.ui.vm.DefaultNoteUiViewModel
import com.edufelip.shared.ui.vm.NotesEvent
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultNoteUiViewModelTest {
    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var vm: DefaultNoteUiViewModel
    private val emptySpans = emptyList<NoteTextSpan>()
    private val emptyAttachments = emptyList<NoteAttachment>()
    private val emptyContent = NoteContent()

    @Before
    fun setup() {
        val repo = FakeDomainRepository()
        val useCases = buildNoteUseCases(
            repo,
            NoteValidationRules(Constants.MAX_TITLE_LENGTH, Constants.MAX_DESCRIPTION_LENGTH),
        )
        vm = DefaultNoteUiViewModel(useCases, dispatcher = mainCoroutineRule.dispatcher)
    }

    @Test
    fun insert_invalid_emptyTitle() = runTest {
        val events = mutableListOf<NotesEvent>()
        val job = launch { vm.events.take(1).collect { events += it } }
        vm.insert("", "desc", emptySpans, emptyAttachments, null, emptyContent)
        advanceUntilIdle()
        job.cancel()
        assertThat(events.single()).isInstanceOf(NotesEvent.ValidationFailed::class.java)
    }

    @Test
    fun insert_success_and_observeNotes() = runTest {
        vm.insert("Title", "Desc", emptySpans, emptyAttachments, null, emptyContent)
        val notes = vm.state.first().notes
        assertThat(notes).hasSize(1)
        assertThat(notes.first().title).isEqualTo("Title")
    }

    @Test
    fun update_invalid_tooLongTitle() = runTest {
        vm.insert("T", "D", emptySpans, emptyAttachments, null, emptyContent)
        advanceUntilIdle()
        val existing = vm.state.first().notes.first()
        val longTitle = "x".repeat(Constants.MAX_TITLE_LENGTH + 1)
        val events = mutableListOf<NotesEvent>()
        val job = launch { vm.events.take(1).collect { events += it } }
        vm.update(existing.id, longTitle, existing.description, false, emptySpans, emptyAttachments, existing.folderId, emptyContent)
        advanceUntilIdle()
        job.cancel()
        assertThat(events.single()).isInstanceOf(NotesEvent.ValidationFailed::class.java)
    }

    @Test
    fun setDeleted_and_restore() = runTest {
        vm.insert("To Delete", "D", emptySpans, emptyAttachments, null, emptyContent)
        advanceUntilIdle()
        val note = vm.state.first().notes.first()
        vm.setDeleted(note.id, true)
        advanceUntilIdle()
        val trash1 = vm.state.first().trash
        assertThat(trash1.map { it.id }).contains(note.id)
        vm.setDeleted(note.id, false)
        advanceUntilIdle()
        val trash2 = vm.state.first().trash
        assertThat(trash2.map { it.id }).doesNotContain(note.id)
    }

    @Test
    fun setDeleted_emitsSyncRequested_whenSyncAfterTrue() = runTest {
        vm.insert("Sync", "Me", emptySpans, emptyAttachments, null, emptyContent)
        advanceUntilIdle()
        val note = vm.state.first().notes.first()

        val events = mutableListOf<NotesEvent>()
        val job = launch { vm.events.take(1).collect { events += it } }

        vm.setDeleted(note.id, true, syncAfter = true)
        advanceUntilIdle()
        job.cancel()

        assertThat(events.single()).isEqualTo(NotesEvent.SyncRequested)
    }

    @Test
    fun createFolder_emitsSyncRequested_andUpdatesState() = runTest {
        val events = mutableListOf<NotesEvent>()
        val job = launch { vm.events.take(1).collect { events += it } }

        vm.createFolder("Work", syncAfter = true)
        advanceUntilIdle()
        job.cancel()

        val folders = vm.state.first().folders
        assertThat(folders).hasSize(1)
        assertThat(folders.first().name).isEqualTo("Work")
        assertThat(events.single()).isEqualTo(NotesEvent.SyncRequested)
    }

    @Test
    fun deleteFolder_emitsSyncRequested_andClearsFolder() = runTest {
        vm.createFolder("Temp")
        advanceUntilIdle()
        val folderId = vm.state.first().folders.first().id

        val events = mutableListOf<NotesEvent>()
        val job = launch { vm.events.take(1).collect { events += it } }
        vm.deleteFolder(folderId, syncAfter = true)
        advanceUntilIdle()
        job.cancel()

        assertThat(vm.state.first().folders).isEmpty()
        assertThat(events.single()).isEqualTo(NotesEvent.SyncRequested)
    }
}
