package com.edufelip.shared.data.sync

import com.edufelip.shared.data.cloud.CloudNotesDataSource
import com.edufelip.shared.data.db.*
import com.edufelip.shared.data.storage.RemoteAttachmentStorage
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.toJson
import com.edufelip.shared.security.NoteCipher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

private val TEST_KEY = ByteArray(32) { index -> (index * 73 % 256).toByte() }

@OptIn(ExperimentalCoroutinesApi::class)
class NotesSyncManagerTest {
    private val activeScopes = mutableListOf<CoroutineScope>()

    @AfterTest
    fun tearDown() {
        NoteCipher.clearKeyOverride()
        activeScopes.forEach { it.cancel() }
        activeScopes.clear()
    }

    private fun TestScope.createSyncManager(
        db: NoteDatabase,
        cloud: CloudNotesDataSource,
        users: FakeCurrentUserProvider,
        storage: RemoteAttachmentStorage = FakeRemoteAttachmentStorage(),
    ): NotesSyncManager {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(SupervisorJob() + dispatcher)
        activeScopes += scope
        return NotesSyncManager(db, scope, cloud, users, storage)
    }

    @Test
    fun syncUploadsDirtyLocalNotesAndClearsDirtyFlag() = runTest {
        NoteCipher.overrideKeyForTests(TEST_KEY)
        val driver = TestNoteDriver()
        val db = NoteDatabase(driver)
        val cloud = FakeCloudNotesDataSource()
        val users = FakeCurrentUserProvider()
        val storage = FakeRemoteAttachmentStorage()
        val syncManager = createSyncManager(db, cloud, users, storage)
        driver.seed(
            dbNote(
                id = 1,
                title = "Local draft",
                updatedAt = 100,
                createdAt = 50,
                dirty = true,
            ),
        )

        syncManager.start()
        users.setCurrentUser("user-1")
        advanceUntilIdle()
        syncManager.syncLocalToRemoteOnly()
        advanceUntilIdle()

        assertEquals(1, cloud.upsertCalls.size)
        val (uid, note) = cloud.upsertCalls.first()
        assertEquals("user-1", uid)
        assertEquals(1, note.id)
        assertEquals("Local draft", note.title)
        assertFalse(driver.requireNote(1).local_dirty == 1L, "local_dirty should be cleared after server acknowledgement")
    }

    @Test
    fun remoteNewerNoteOverwritesLocalData() = runTest {
        NoteCipher.overrideKeyForTests(TEST_KEY)
        val driver = TestNoteDriver()
        val db = NoteDatabase(driver)
        val cloud = FakeCloudNotesDataSource()
        val users = FakeCurrentUserProvider()
        val syncManager = createSyncManager(db, cloud, users)
        driver.seed(
            dbNote(
                id = 2,
                title = "Older local",
                updatedAt = 100,
                createdAt = 10,
                dirty = false,
            ),
        )
        cloud.seedRemote(
            "user-1",
            listOf(
                Note(
                    id = 2,
                    title = "Remote fresher",
                    description = "server",
                    deleted = false,
                    createdAt = 10,
                    updatedAt = 200,
                ),
            ),
        )

        syncManager.start()
        users.setCurrentUser("user-1")
        syncManager.syncNow("user-1")
        advanceUntilIdle()

        val stored = driver.requireNote(2)
        assertEquals("Remote fresher", decryptField(stored.title))
        assertEquals("server", decryptField(stored.description))
        assertEquals(200, stored.updated_at)
    }

    @Test
    fun remoteFoldersPopulateLocalTable() = runTest {
        NoteCipher.overrideKeyForTests(TEST_KEY)
        val driver = TestNoteDriver()
        val db = NoteDatabase(driver)
        val cloud = FakeCloudNotesDataSource()
        val users = FakeCurrentUserProvider()
        val syncManager = createSyncManager(db, cloud, users)
        cloud.seedRemote(
            uid = "user-1",
            notes = listOf(
                Note(
                    id = 5,
                    title = "Foldered note",
                    description = "",
                    deleted = false,
                    createdAt = 10,
                    updatedAt = 20,
                    folderId = 42,
                ),
            ),
            folders = listOf(
                Folder(
                    id = 42,
                    name = "Projects",
                    createdAt = 1,
                    updatedAt = 20,
                ),
            ),
        )

        syncManager.start()
        users.setCurrentUser("user-1")
        syncManager.syncNow("user-1")
        advanceUntilIdle()

        val storedFolder = driver.getFolder(42) ?: error("Folder not synced")
        assertEquals("Projects", storedFolder.name)
        assertEquals(0, storedFolder.deleted)
    }

    @Test
    fun missingRemoteFolderCreatesPlaceholder() = runTest {
        NoteCipher.overrideKeyForTests(TEST_KEY)
        val driver = TestNoteDriver()
        val db = NoteDatabase(driver)
        val cloud = FakeCloudNotesDataSource()
        val users = FakeCurrentUserProvider()
        val syncManager = createSyncManager(db, cloud, users)
        val folderId = 77L
        cloud.seedRemote(
            uid = "user-1",
            notes = listOf(
                Note(
                    id = 9,
                    title = "Dangling",
                    description = "",
                    deleted = false,
                    createdAt = 5,
                    updatedAt = 15,
                    folderId = folderId,
                ),
            ),
        )

        syncManager.start()
        users.setCurrentUser("user-1")
        syncManager.syncNow("user-1")
        advanceUntilIdle()

        val storedFolder = driver.getFolder(folderId) ?: error("Placeholder not created")
        assertEquals("Untitled Folder", storedFolder.name)
        assertEquals(0L, storedFolder.local_dirty)
    }

    @Test
    fun pendingNoteDeletionsArePushedToRemote() = runTest {
        NoteCipher.overrideKeyForTests(TEST_KEY)
        val driver = TestNoteDriver()
        val db = NoteDatabase(driver)
        val cloud = FakeCloudNotesDataSource()
        val users = FakeCurrentUserProvider()
        val storage = FakeRemoteAttachmentStorage()
        val syncManager = createSyncManager(db, cloud, users, storage)
        val noteId = 5
        cloud.seedRemote(
            uid = "user-1",
            notes = listOf(
                Note(
                    id = noteId,
                    title = "Remote note",
                    description = "",
                    deleted = false,
                    createdAt = 1,
                    updatedAt = 2,
                ),
            ),
        )

        syncManager.start()
        users.setCurrentUser("user-1")
        advanceUntilIdle()

        val stableId = "stable-$noteId"
        val paths = listOf("images/user-1/$stableId/file.jpg")
        db.noteQueries.insertPendingNoteDeletion(
            id = noteId.toLong(),
            deleted_at = 10,
            stable_id = stableId,
            storage_paths = encodePaths(paths),
        )

        syncManager.syncLocalToRemoteOnly()
        advanceUntilIdle()

        assertFalse(cloud.hasRemoteNote("user-1", noteId))
        assertTrue(db.noteQueries.selectPendingNoteDeletions().executeAsList().isEmpty())
        assertEquals(listOf(paths), storage.deletions)
    }

    @Test
    fun storageFailuresKeepPendingDeletionForRetry() = runTest {
        NoteCipher.overrideKeyForTests(TEST_KEY)
        val driver = TestNoteDriver()
        val db = NoteDatabase(driver)
        val cloud = FakeCloudNotesDataSource()
        val users = FakeCurrentUserProvider()
        val storage = FakeRemoteAttachmentStorage().apply { shouldFail = true }
        val syncManager = createSyncManager(db, cloud, users, storage)
        val noteId = 7

        syncManager.start()
        users.setCurrentUser("user-1")
        advanceUntilIdle()

        db.noteQueries.insertPendingNoteDeletion(
            id = noteId.toLong(),
            deleted_at = 20,
            stable_id = "stable-$noteId",
            storage_paths = encodePaths(listOf("images/user-1/stable-$noteId/file.jpg")),
        )

        syncManager.syncLocalToRemoteOnly()
        advanceUntilIdle()

        val pending = db.noteQueries.selectPendingNoteDeletions().executeAsList()
        assertEquals(1, pending.size)
        assertTrue(storage.deletions.isEmpty())
    }

    @Test
    fun pendingFolderDeletionsArePushedToRemote() = runTest {
        NoteCipher.overrideKeyForTests(TEST_KEY)
        val driver = TestNoteDriver()
        val db = NoteDatabase(driver)
        val cloud = FakeCloudNotesDataSource()
        val users = FakeCurrentUserProvider()
        val syncManager = createSyncManager(db, cloud, users)
        val folderId = 7L
        cloud.seedRemote(
            uid = "user-1",
            notes = emptyList(),
            folders = listOf(
                Folder(
                    id = folderId,
                    name = "Work",
                    createdAt = 1,
                    updatedAt = 2,
                ),
            ),
        )

        syncManager.start()
        users.setCurrentUser("user-1")
        advanceUntilIdle()

        db.noteQueries.insertPendingFolderDeletion(folderId, deleted_at = 5)

        syncManager.syncLocalToRemoteOnly()
        advanceUntilIdle()

        assertFalse(cloud.hasRemoteFolder("user-1", folderId))
        assertTrue(db.noteQueries.selectPendingFolderDeletions().executeAsList().isEmpty())
    }

    @Test
    fun logoutClearsLocalDatabaseAndFolders() = runTest {
        NoteCipher.overrideKeyForTests(TEST_KEY)
        val driver = TestNoteDriver()
        val db = NoteDatabase(driver)
        val cloud = FakeCloudNotesDataSource()
        val users = FakeCurrentUserProvider()
        val syncManager = createSyncManager(db, cloud, users)
        driver.seed(
            dbNote(
                id = 3,
                title = "Persisted note",
                updatedAt = 42,
                createdAt = 1,
                dirty = false,
            ),
        )

        syncManager.start()
        users.setCurrentUser("user-1")
        advanceUntilIdle()

        users.setCurrentUser(null)
        advanceUntilIdle()

        assertTrue(driver.allNotes().isEmpty())
        assertEquals(1, driver.folderClears)
    }

    @Test
    fun reloggingInRehydratesLocalStateFromRemote() = runTest {
        NoteCipher.overrideKeyForTests(TEST_KEY)
        val driver = TestNoteDriver()
        val db = NoteDatabase(driver)
        val cloud = FakeCloudNotesDataSource()
        val users = FakeCurrentUserProvider()
        val syncManager = createSyncManager(db, cloud, users)
        val noteId = 99
        val folderId = 123L
        cloud.seedRemote(
            uid = "user-1",
            notes = listOf(
                Note(
                    id = noteId,
                    title = "Remote only",
                    description = "from cloud",
                    deleted = false,
                    createdAt = 5,
                    updatedAt = 50,
                    folderId = folderId,
                ),
            ),
            folders = listOf(
                Folder(
                    id = folderId,
                    name = "Shared",
                    createdAt = 1,
                    updatedAt = 10,
                ),
            ),
        )

        syncManager.start()
        users.setCurrentUser("user-1")
        syncManager.syncNow("user-1")
        advanceUntilIdle()

        assertEquals("Remote only", decryptField(driver.requireNote(noteId.toLong()).title))
        assertEquals("Shared", driver.getFolder(folderId)?.name)

        users.setCurrentUser(null)
        advanceUntilIdle()
        assertNull(driver.getNote(noteId.toLong()))
        assertNull(driver.getFolder(folderId))

        users.setCurrentUser("user-1")
        syncManager.syncNow("user-1")
        advanceUntilIdle()

        assertEquals("Remote only", decryptField(driver.requireNote(noteId.toLong()).title))
        assertEquals("Shared", driver.getFolder(folderId)?.name)
    }

    @Test
    fun remoteDeletionRemovesLocalNoteWhenNotDirty() = runTest {
        NoteCipher.overrideKeyForTests(TEST_KEY)
        val driver = TestNoteDriver()
        val db = NoteDatabase(driver)
        val cloud = FakeCloudNotesDataSource()
        val users = FakeCurrentUserProvider()
        val syncManager = createSyncManager(db, cloud, users)
        driver.seed(
            dbNote(
                id = 4,
                title = "To be deleted",
                updatedAt = 80,
                createdAt = 20,
                dirty = false,
            ),
        )

        syncManager.start()
        users.setCurrentUser("user-1")
        syncManager.syncNow("user-1")
        advanceUntilIdle()

        cloud.seedRemote("user-1", emptyList())
        cloud.pushRemote("user-1")
        advanceUntilIdle()

        assertNull(driver.getNote(4))
    }
}

private fun dbNote(
    id: Int,
    title: String,
    description: String = "",
    updatedAt: Long,
    createdAt: Long,
    dirty: Boolean,
    deleted: Boolean = false,
    stableId: String = "note-$id",
): com.edufelip.shared.db.Note {
    // println("dbNote: id=$id title=$title updatedAt=$updatedAt createdAt=$createdAt dirty=$dirty deleted=$deleted stableId=$stableId")
    val encryptedTitle = encryptField(title)
    val encryptedDescription = encryptField(description)
    val emptyJson = encryptField("[]")
    val contentJson = encryptField(NoteContent().toJson())
    return com.edufelip.shared.db.Note(
        id = id.toLong(),
        title = encryptedTitle,
        description = encryptedDescription,
        description_spans = emptyJson,
        attachments = emptyJson,
        blocks = "[]",
        content_json = contentJson,
        deleted = if (deleted) 1 else 0,
        created_at = createdAt,
        updated_at = updatedAt,
        local_dirty = if (dirty) 1 else 0,
        local_updated_at = if (dirty) updatedAt else 0,
        folder_id = null,
        stable_id = stableId,
    )
}
