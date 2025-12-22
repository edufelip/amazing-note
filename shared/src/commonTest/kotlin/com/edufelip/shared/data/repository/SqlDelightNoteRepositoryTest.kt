package com.edufelip.shared.data.repository

import com.edufelip.shared.data.db.TestNoteDriver
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.TextBlock
import com.edufelip.shared.security.NoteCipher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val TEST_KEY = ByteArray(32) { index -> (index * 73 % 256).toByte() }

class SqlDelightNoteRepositoryTest {

    private lateinit var driver: TestNoteDriver

    @BeforeTest
    fun setUp() {
        NoteCipher.overrideKeyForTests(TEST_KEY)
        driver = TestNoteDriver()
    }

    private fun runRepositoryTest(block: suspend (SqlDelightNoteRepository) -> Unit) = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val database = NoteDatabase(driver)
        val repository = SqlDelightNoteRepository(database, testDispatcher)
        block(repository)
    }

    @Test
    fun insertNotePersistsInDatabase() = runRepositoryTest { repository ->
        repository.insert(
            title = "New Note",
            description = "Some content",
            folderId = null,
            spans = emptyList(),
            attachments = emptyList(),
            content = NoteContent(),
            stableId = "stable-1",
        )

        val notes = repository.notes().first()
        assertEquals(1, notes.size)
        assertEquals("New Note", notes[0].title)
        assertEquals("stable-1", notes[0].stableId)
    }

    @Test
    fun updateNoteModifiesExistingData() = runRepositoryTest { repository ->
        repository.insert("Initial", "", null, emptyList(), emptyList(), NoteContent(), "stable-1")
        val initialNotes = repository.notes().first()
        val id = initialNotes[0].id

        repository.update(
            id = id,
            title = "Updated",
            description = "New desc",
            deleted = false,
            folderId = null,
            spans = emptyList(),
            attachments = emptyList(),
            content = NoteContent(),
        )

        val updatedNotes = repository.notes().first()
        assertEquals("Updated", updatedNotes[0].title)
    }

    @Test
    fun setDeletedMovesNoteToTrash() = runRepositoryTest { repository ->
        repository.insert("To Delete", "", null, emptyList(), emptyList(), NoteContent(), "stable-1")
        val noteId = repository.notes().first()[0].id

        repository.setDeleted(noteId, true)

        assertTrue(repository.notes().first().isEmpty())
        assertEquals(1, repository.trash().first().size)
        assertEquals("To Delete", repository.trash().first()[0].title)
    }

    @Test
    fun createFolderReturnsNewId() = runRepositoryTest { repository ->
        val id = repository.insertFolder("My Folder")
        val folders = repository.folders().first()
        assertEquals(1, folders.size)
        assertEquals("My Folder", folders[0].name)
        assertEquals(id, folders[0].id)
    }

    @Test
    fun deleteNoteRemovesItPermanently() = runRepositoryTest { repository ->
        repository.insert("To Delete", "", null, emptyList(), emptyList(), NoteContent(), "s1")
        val id = repository.notes().first()[0].id
        repository.delete(id)
        assertTrue(repository.notes().first().isEmpty())
    }

    @Test
    fun restoreNoteClearsDeletedFlag() = runRepositoryTest { repository ->
        repository.insert("To Restore", "", null, emptyList(), emptyList(), NoteContent(), "s1")
        val id = repository.notes().first()[0].id
        repository.setDeleted(id, true)
        assertTrue(repository.notes().first().isEmpty())

        repository.setDeleted(id, false)
        assertEquals(1, repository.notes().first().size)
    }

    @Test
    fun updateModifiesData() = runRepositoryTest { repository ->
        repository.insert("Note", "", null, emptyList(), emptyList(), NoteContent(), "s1")
        val id = repository.notes().first()[0].id
        val newContent = NoteContent(blocks = listOf(TextBlock(text = "Updated")))

        repository.update(
            id = id,
            title = "Note",
            description = "",
            deleted = false,
            folderId = null,
            spans = emptyList(),
            attachments = emptyList(),
            content = newContent,
        )

        val note = repository.notes().first()[0]
        assertEquals(newContent, note.content)
    }

    @Test
    fun deleteFolderRemovesFolder() = runRepositoryTest { repository ->
        val id = repository.insertFolder("Work")
        assertEquals(1, repository.folders().first().size)

        repository.deleteFolder(id)
        assertTrue(repository.folders().first().isEmpty())
    }

    @Test
    fun assignToFolderUpdatesRelation() = runRepositoryTest { repository ->
        repository.insert("Note", "", null, emptyList(), emptyList(), NoteContent(), "s1")
        val noteId = repository.notes().first()[0].id
        val folderId = repository.insertFolder("Folder")

        repository.assignToFolder(noteId, folderId)

        assertEquals(folderId, repository.notes().first()[0].folderId)
    }
}
