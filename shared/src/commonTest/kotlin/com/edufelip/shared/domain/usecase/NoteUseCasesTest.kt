package com.edufelip.shared.domain.usecase

import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.NoteAttachment
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.NoteTextSpan
import com.edufelip.shared.domain.repository.NoteRepository
import com.edufelip.shared.domain.validation.NoteActionResult
import com.edufelip.shared.domain.validation.NoteValidationRules
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NoteUseCasesTest {

    private lateinit var repository: FakeNoteRepository
    private lateinit var useCases: NoteUseCases

    @BeforeTest
    fun setUp() {
        repository = FakeNoteRepository()
        useCases = buildNoteUseCases(repository)
    }

    @Test
    fun observeNotesDelegatesToRepository() = runTest {
        val notes = listOf(createNote(1))
        repository.notesFlow.value = notes
        val result = useCases.observeNotes().first()
        assertEquals(notes, result)
    }

    @Test
    fun insertNoteValidatesAndCallsRepository() = runTest {
        val result = useCases.insertNote("Title", "Desc", null, emptyList(), emptyList())
        assertTrue(result is NoteActionResult.Success)
        assertEquals(1, repository.insertCalls.size)
        assertEquals("Title", repository.insertCalls[0].title)
    }

    @Test
    fun insertNoteInvalidatesLongTitle() = runTest {
        val rules = NoteValidationRules(maxTitleLength = 5)
        val useCasesWithRules = buildNoteUseCases(repository, rules)
        val result = useCasesWithRules.insertNote("Too Long Title", "Desc", null, emptyList(), emptyList())
        assertTrue(result is NoteActionResult.Invalid)
        assertEquals(0, repository.insertCalls.size)
    }

    @Test
    fun updateNoteValidatesAndCallsRepository() = runTest {
        val result = useCases.updateNote(1, "Title", "Desc", false, null, emptyList(), emptyList())
        assertTrue(result is NoteActionResult.Success)
        assertEquals(1, repository.updateCalls.size)
        assertEquals(1, repository.updateCalls[0].id)
    }

    @Test
    fun setDeletedCallsRepository() = runTest {
        useCases.setDeleted(1, true)
        assertEquals(listOf(1 to true), repository.setDeletedCalls)
    }

    @Test
    fun createFolderCallsRepository() = runTest {
        repository.nextFolderId = 42L
        val id = useCases.createFolder("Work")
        assertEquals(42L, id)
        assertEquals(listOf("Work"), repository.insertFolderCalls)
    }

    private fun createNote(id: Int) = Note(
        id = id,
        title = "Note $id",
        description = "",
        deleted = false,
        createdAt = 0,
        updatedAt = 0,
    )

    private class FakeNoteRepository : NoteRepository {
        val notesFlow = MutableStateFlow<List<Note>>(emptyList())
        val trashFlow = MutableStateFlow<List<Note>>(emptyList())
        val foldersFlow = MutableStateFlow<List<Folder>>(emptyList())

        data class InsertCall(val title: String, val description: String, val folderId: Long?, val spans: List<NoteTextSpan>, val attachments: List<NoteAttachment>, val content: NoteContent, val stableId: String?)
        val insertCalls = mutableListOf<InsertCall>()

        data class UpdateCall(val id: Int, val title: String, val description: String, val deleted: Boolean, val folderId: Long?, val spans: List<NoteTextSpan>, val attachments: List<NoteAttachment>, val content: NoteContent)
        val updateCalls = mutableListOf<UpdateCall>()

        val setDeletedCalls = mutableListOf<Pair<Int, Boolean>>()
        val deleteCalls = mutableListOf<Int>()
        val assignToFolderCalls = mutableListOf<Pair<Int, Long?>>()
        val insertFolderCalls = mutableListOf<String>()
        val renameFolderCalls = mutableListOf<Pair<Long, String>>()
        val deleteFolderCalls = mutableListOf<Long>()

        var nextFolderId = 0L

        override fun notes(): Flow<List<Note>> = notesFlow
        override fun trash(): Flow<List<Note>> = trashFlow
        override fun notesByFolder(folderId: Long): Flow<List<Note>> = notesFlow // Simplified
        override fun notesWithoutFolder(): Flow<List<Note>> = notesFlow // Simplified
        override fun folders(): Flow<List<Folder>> = foldersFlow

        override suspend fun insert(title: String, description: String, folderId: Long?, spans: List<NoteTextSpan>, attachments: List<NoteAttachment>, content: NoteContent, stableId: String?) {
            insertCalls += InsertCall(title, description, folderId, spans, attachments, content, stableId)
        }

        override suspend fun update(id: Int, title: String, description: String, deleted: Boolean, folderId: Long?, spans: List<NoteTextSpan>, attachments: List<NoteAttachment>, content: NoteContent) {
            updateCalls += UpdateCall(id, title, description, deleted, folderId, spans, attachments, content)
        }

        override suspend fun setDeleted(id: Int, deleted: Boolean) {
            setDeletedCalls += id to deleted
        }

        override suspend fun delete(id: Int) {
            deleteCalls += id
        }

        override suspend fun assignToFolder(id: Int, folderId: Long?) {
            assignToFolderCalls += id to folderId
        }

        override suspend fun insertFolder(name: String): Long {
            insertFolderCalls += name
            return nextFolderId
        }

        override suspend fun renameFolder(id: Long, name: String) {
            renameFolderCalls += id to name
        }

        override suspend fun deleteFolder(id: Long) {
            deleteFolderCalls += id
        }
    }
}
