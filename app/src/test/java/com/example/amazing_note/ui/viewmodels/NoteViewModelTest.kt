package com.example.amazing_note.ui.viewmodels

import com.example.amazing_note.data.models.Note
import com.example.amazing_note.data.models.Priority
import com.example.amazing_note.data.repositories.FakeNoteRepository
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NoteViewModelTest {
    private lateinit var viewModel: NoteViewModel

    @Before
    fun setup() {
        viewModel = NoteViewModel(FakeNoteRepository())
    }

    @Test
    fun `Should Insert Note`() {
        val note = Note(0, "Random Title", Priority.HIGH, "Random Description", false)
        viewModel.insertNote(note)
    }
}