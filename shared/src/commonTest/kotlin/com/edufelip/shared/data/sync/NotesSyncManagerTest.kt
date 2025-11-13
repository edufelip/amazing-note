package com.edufelip.shared.data.sync

import app.cash.sqldelight.Query
import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import com.edufelip.shared.data.cloud.CloudNotesDataSource
import com.edufelip.shared.data.cloud.CurrentUserProvider
import com.edufelip.shared.data.db.decryptField
import com.edufelip.shared.data.db.encryptField
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
        syncManager.syncNow()
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
        syncManager.syncNow()
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
        syncManager.syncNow()
        advanceUntilIdle()

        val storedFolder = driver.getFolder(folderId) ?: error("Placeholder not created")
        assertEquals("Untitled Folder", storedFolder.name)
        assertEquals(1L, storedFolder.local_dirty)
    }

    @Test
    fun pendingNoteDeletionsArePushedToRemote() = runTest {
        NoteCipher.overrideKeyForTests(TEST_KEY)
        val driver = TestNoteDriver()
        val db = NoteDatabase(driver)
        val cloud = FakeCloudNotesDataSource()
        val users = FakeCurrentUserProvider()
        val syncManager = createSyncManager(db, cloud, users)
        val noteId = 42
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
        syncManager.syncNow()
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

private class FakeCloudNotesDataSource : CloudNotesDataSource {
    private val remoteNotesByUser = mutableMapOf<String, MutableMap<Int, Note>>()
    private val remoteFoldersByUser = mutableMapOf<String, MutableMap<Long, Folder>>()
    private val flows = mutableMapOf<String, MutableStateFlow<RemoteSyncPayload>>()

    val upsertCalls = mutableListOf<Pair<String, Note>>()
    val deleteNoteCalls = mutableListOf<Pair<String, Int>>()
    val deleteFolderCalls = mutableListOf<Pair<String, Long>>()

    override fun observe(uid: String): Flow<RemoteSyncPayload> = flows.getOrPut(uid) {
        MutableStateFlow(snapshot(uid))
    }

    override suspend fun getAll(uid: String): RemoteSyncPayload = snapshot(uid)

    override suspend fun upsert(uid: String, note: Note) {
        val userNotes = remoteNotesByUser.getOrPut(uid) { mutableMapOf() }
        val serverNote = note.copy(updatedAt = note.updatedAt + 5, dirty = false)
        userNotes[note.id] = serverNote
        upsertCalls += uid to serverNote
        pushRemote(uid)
    }

    override suspend fun delete(uid: String, id: Int) {
        remoteNotesByUser[uid]?.remove(id)
        deleteNoteCalls += uid to id
        pushRemote(uid)
    }

    override suspend fun upsertPreserveUpdatedAt(uid: String, note: Note) {
        val userNotes = remoteNotesByUser.getOrPut(uid) { mutableMapOf() }
        userNotes[note.id] = note.copy(dirty = false)
        pushRemote(uid)
    }

    override suspend fun upsertFolder(uid: String, folder: Folder) {
        val userFolders = remoteFoldersByUser.getOrPut(uid) { mutableMapOf() }
        userFolders[folder.id] = folder.copy(dirty = false)
        pushRemote(uid)
    }

    override suspend fun deleteFolder(uid: String, id: Long) {
        remoteFoldersByUser[uid]?.remove(id)
        deleteFolderCalls += uid to id
        pushRemote(uid)
    }

    fun seedRemote(uid: String, notes: List<Note>, folders: List<Folder> = emptyList()) {
        remoteNotesByUser[uid] = notes.associateBy { it.id }.toMutableMap()
        remoteFoldersByUser[uid] = folders.associateBy { it.id }.toMutableMap()
        pushRemote(uid)
    }

    fun pushRemote(uid: String) {
        val state = flows.getOrPut(uid) { MutableStateFlow(snapshot(uid)) }
        state.value = snapshot(uid)
    }

    private fun snapshot(uid: String): RemoteSyncPayload {
        val notes = remoteNotesByUser[uid]?.values?.sortedBy { it.updatedAt } ?: emptyList()
        val folders = remoteFoldersByUser[uid]?.values?.sortedBy { it.updatedAt } ?: emptyList()
        return RemoteSyncPayload(notes, folders)
    }

    fun hasRemoteNote(uid: String, id: Int): Boolean = remoteNotesByUser[uid]?.containsKey(id) == true

    fun hasRemoteFolder(uid: String, id: Long): Boolean = remoteFoldersByUser[uid]?.containsKey(id) == true
}

private class FakeCurrentUserProvider(
    initial: String? = null,
) : CurrentUserProvider {
    private val state = MutableStateFlow(initial)
    override val uid: Flow<String?> = state

    fun setCurrentUser(uid: String?) {
        state.value = uid
    }
}

private class FakeRemoteAttachmentStorage : RemoteAttachmentStorage {
    val deletions = mutableListOf<List<String>>()
    var shouldFail: Boolean = false

    override suspend fun deleteNoteAttachments(paths: List<String>) {
        if (shouldFail) throw IllegalStateException("storage failure")
        deletions += paths
    }
}

private class TestNoteDriver : SqlDriver {
    private data class PendingNoteDeletion(val deletedAt: Long, val stableId: String, val storagePaths: String)
    private val notes = linkedMapOf<Long, com.edufelip.shared.db.Note>()
    private val folders = linkedHashMapOf<Long, com.edufelip.shared.db.Folder>()
    private val pendingNoteDeletions = linkedMapOf<Long, PendingNoteDeletion>()
    private val pendingFolderDeletions = linkedMapOf<Long, Long>()
    private var lastInsertedFolderId: Long = 0L
    var folderClears: Int = 0

    fun seed(note: com.edufelip.shared.db.Note) {
        notes[note.id] = note
    }

    fun allNotes(): Collection<com.edufelip.shared.db.Note> = notes.values

    fun getNote(id: Long): com.edufelip.shared.db.Note? = notes[id]

    fun requireNote(id: Long): com.edufelip.shared.db.Note = getNote(id) ?: error("Missing note $id")

    fun getFolder(id: Long): com.edufelip.shared.db.Folder? = folders[id]

    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (SqlCursor) -> QueryResult<R>,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<R> {
        val normalized = sql.normalizeSql()
        val statement = binders?.let { TestPreparedStatement().apply(it) }
        return when (normalized) {
            SELECT_NOTES_ACTIVE -> mapper(NoteCursor(notes.values.filter { it.deleted == 0L }.sortedWith(DESCENDING_NOTES)))
            SELECT_NOTES_DELETED -> mapper(NoteCursor(notes.values.filter { it.deleted == 1L }.sortedWith(DESCENDING_NOTES)))
            SELECT_NOTES_DIRTY -> mapper(NoteCursor(notes.values.filter { it.local_dirty == 1L }.sortedWith(DESCENDING_NOTES)))
            SELECT_FOLDERS_ACTIVE -> mapper(FolderCursor(folders.values.filter { it.deleted == 0L }.sortedWith(DESCENDING_FOLDERS)))
            SELECT_FOLDERS_ALL -> mapper(FolderCursor(folders.values.sortedWith(DESCENDING_FOLDERS)))
            SELECT_FOLDERS_DIRTY -> mapper(FolderCursor(folders.values.filter { it.local_dirty == 1L }.sortedWith(DESCENDING_FOLDERS)))
            SELECT_FOLDER_BY_ID -> {
                val id = statement?.long(0) ?: 0L
                mapper(FolderCursor(listOfNotNull(folders[id])))
            }
            SELECT_LAST_INSERT_ID -> mapper(ScalarCursor(lastInsertedFolderId))
            SELECT_PENDING_NOTE_DELETIONS -> mapper(
                PendingDeletionCursor(
                    pendingNoteDeletions.map { (id, data) -> PendingDeletionRow(id, data.deletedAt, data.stableId, data.storagePaths) },
                ),
            )
            SELECT_PENDING_FOLDER_DELETIONS -> mapper(
                PendingDeletionCursor(
                    pendingFolderDeletions.map { (id, deletedAt) -> PendingDeletionRow(id, deletedAt, null, "[]") },
                ),
            )
            else -> error("Unhandled query: $sql")
        }
    }

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<Long> {
        val normalized = sql.normalizeSql()
        val statement = TestPreparedStatement().apply { binders?.invoke(this) }
        when (normalized) {
            INSERT_NOTE_WITH_ID -> {
                val id = statement.long(0) ?: error("id required")
                notes[id] = com.edufelip.shared.db.Note(
                    id = id,
                    title = statement.string(1).orEmpty(),
                    description = statement.string(2).orEmpty(),
                    description_spans = statement.string(3).orEmpty(),
                    attachments = statement.string(4).orEmpty(),
                    blocks = statement.string(5).orEmpty(),
                    content_json = statement.string(6),
                    deleted = statement.long(7) ?: 0,
                    created_at = statement.long(8) ?: 0,
                    updated_at = statement.long(9) ?: 0,
                    local_dirty = 0,
                    local_updated_at = 0,
                    folder_id = statement.long(10),
                    stable_id = statement.string(11).orEmpty(),
                )
            }

            UPDATE_NOTE_FROM_REMOTE -> {
                val id = statement.long(10) ?: error("id required")
                val existing = notes[id] ?: return QueryResult.Value(0)
                notes[id] = existing.copy(
                    title = statement.string(0).orEmpty(),
                    description = statement.string(1).orEmpty(),
                    description_spans = statement.string(2).orEmpty(),
                    attachments = statement.string(3).orEmpty(),
                    blocks = statement.string(4).orEmpty(),
                    content_json = statement.string(5),
                    deleted = statement.long(6) ?: existing.deleted,
                    updated_at = statement.long(7) ?: existing.updated_at,
                    local_dirty = 0,
                    folder_id = statement.long(8),
                    stable_id = statement.string(9) ?: existing.stable_id,
                )
            }

            DELETE_NOTE_BY_ID -> {
                val id = statement.long(0) ?: return QueryResult.Value(0)
                notes.remove(id)
            }

            DELETE_ALL_NOTES -> notes.clear()
            CLEAR_NOTE_DIRTY -> {
                val id = statement.long(0) ?: return QueryResult.Value(0)
                notes[id]?.let { notes[id] = it.copy(local_dirty = 0) }
            }

            DELETE_ALL_FOLDERS -> {
                folders.clear()
                folderClears += 1
            }

            INSERT_FOLDER_WITH_ID -> {
                val id = statement.long(0) ?: error("folder id required")
                val row = com.edufelip.shared.db.Folder(
                    id = id,
                    name = statement.string(1).orEmpty(),
                    created_at = statement.long(2) ?: 0,
                    updated_at = statement.long(3) ?: 0,
                    deleted = statement.long(4) ?: 0,
                    local_dirty = statement.long(5) ?: 0,
                    local_updated_at = statement.long(6) ?: 0,
                )
                folders[id] = row
                lastInsertedFolderId = id
            }

            UPDATE_FOLDER_FROM_REMOTE -> {
                val id = statement.long(3) ?: error("folder id required")
                val existing = folders[id] ?: return QueryResult.Value(0)
                folders[id] = existing.copy(
                    name = statement.string(0).orEmpty(),
                    updated_at = statement.long(1) ?: existing.updated_at,
                    deleted = statement.long(2) ?: existing.deleted,
                    local_dirty = 0,
                )
            }

            DELETE_FOLDER_BY_ID -> {
                val id = statement.long(0) ?: return QueryResult.Value(0)
                folders.remove(id)
            }

            CLEAR_FOLDER_DIRTY -> {
                val id = statement.long(0) ?: return QueryResult.Value(0)
                folders[id]?.let { folders[id] = it.copy(local_dirty = 0) }
            }

            MARK_FOLDER_DELETED -> {
                val id = statement.long(2) ?: return QueryResult.Value(0)
                val existing = folders[id]
                if (existing != null) {
                    folders[id] = existing.copy(
                        deleted = 1,
                        updated_at = statement.long(0) ?: existing.updated_at,
                        local_dirty = 1,
                        local_updated_at = statement.long(1) ?: existing.local_updated_at,
                    )
                }
            }

            INSERT_PENDING_NOTE_DELETION -> {
                val id = statement.long(0) ?: error("note id required")
                val deletedAt = statement.long(1) ?: 0L
                val stableId = statement.string(2) ?: id.toString()
                val storagePaths = statement.string(3) ?: "[]"
                pendingNoteDeletions[id] = PendingNoteDeletion(deletedAt, stableId, storagePaths)
            }

            DELETE_PENDING_NOTE_DELETION -> {
                val id = statement.long(0) ?: return QueryResult.Value(0)
                pendingNoteDeletions.remove(id)
            }

            INSERT_PENDING_FOLDER_DELETION -> {
                val id = statement.long(0) ?: error("folder id required")
                val deletedAt = statement.long(1) ?: 0L
                pendingFolderDeletions[id] = deletedAt
            }

            DELETE_PENDING_FOLDER_DELETION -> {
                val id = statement.long(0) ?: return QueryResult.Value(0)
                pendingFolderDeletions.remove(id)
            }

            DELETE_ALL_PENDING_NOTE_DELETIONS -> pendingNoteDeletions.clear()

            DELETE_ALL_PENDING_FOLDER_DELETIONS -> pendingFolderDeletions.clear()

            else -> error("Unhandled statement: $sql")
        }
        return QueryResult.Value(0)
    }

    override fun newTransaction(): QueryResult<Transacter.Transaction> = QueryResult.Value(object : Transacter.Transaction() {
        override val enclosingTransaction: Transacter.Transaction? = null
        override fun endTransaction(successful: Boolean): QueryResult<Unit> = QueryResult.Value(Unit)
    })

    override fun currentTransaction(): Transacter.Transaction? = null

    override fun addListener(vararg queryKeys: String, listener: Query.Listener) {}

    override fun removeListener(vararg queryKeys: String, listener: Query.Listener) {}

    override fun notifyListeners(vararg queryKeys: String) {}

    override fun close() {}

    companion object {
        private const val SELECT_NOTES_ACTIVE = "select * from note where deleted = 0 order by updated_at desc, id desc"
        private const val SELECT_NOTES_DELETED = "select * from note where deleted = 1 order by updated_at desc, id desc"
        private const val SELECT_NOTES_DIRTY = "select * from note where local_dirty = 1 order by updated_at desc, id desc"
        private const val SELECT_FOLDERS_ACTIVE = "select * from folder where deleted = 0 order by updated_at desc, id desc"
        private const val SELECT_FOLDERS_ALL = "select * from folder order by updated_at desc, id desc"
        private const val SELECT_FOLDERS_DIRTY = "select * from folder where local_dirty = 1 order by updated_at desc, id desc"
        private const val SELECT_FOLDER_BY_ID = "select * from folder where id = ?"
        private const val SELECT_LAST_INSERT_ID = "select last_insert_rowid()"
        private const val INSERT_NOTE_WITH_ID = "insert into note(id, title, description, description_spans, attachments, blocks, content_json, deleted, created_at, updated_at, folder_id, stable_id) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        private const val UPDATE_NOTE_FROM_REMOTE = "update note set title = ?, description = ?, description_spans = ?, attachments = ?, blocks = ?, content_json = ?, deleted = ?, updated_at = ?, local_dirty = 0, folder_id = ?, stable_id = ? where id = ?"
        private const val DELETE_NOTE_BY_ID = "delete from note where id = ?"
        private const val DELETE_ALL_NOTES = "delete from note"
        private const val CLEAR_NOTE_DIRTY = "update note set local_dirty = 0 where id = ?"
        private const val DELETE_ALL_FOLDERS = "delete from folder"
        private const val INSERT_FOLDER_WITH_ID = "insert into folder(id, name, created_at, updated_at, deleted, local_dirty, local_updated_at) values (?, ?, ?, ?, ?, ?, ?)"
        private const val UPDATE_FOLDER_FROM_REMOTE = "update folder set name = ?, updated_at = ?, deleted = ?, local_dirty = 0 where id = ?"
        private const val DELETE_FOLDER_BY_ID = "delete from folder where id = ?"
        private const val CLEAR_FOLDER_DIRTY = "update folder set local_dirty = 0 where id = ?"
        private const val MARK_FOLDER_DELETED = "update folder set deleted = 1, updated_at = ?, local_dirty = 1, local_updated_at = ? where id = ?"
        private const val SELECT_PENDING_NOTE_DELETIONS = "select * from note_pending_deletion order by deleted_at asc"
        private const val SELECT_PENDING_FOLDER_DELETIONS = "select * from folder_pending_deletion order by deleted_at asc"
        private const val INSERT_PENDING_NOTE_DELETION = "insert or replace into note_pending_deletion(id, deleted_at, stable_id, storage_paths) values (?, ?, ?, ?)"
        private const val DELETE_PENDING_NOTE_DELETION = "delete from note_pending_deletion where id = ?"
        private const val INSERT_PENDING_FOLDER_DELETION = "insert or replace into folder_pending_deletion(id, deleted_at) values (?, ?)"
        private const val DELETE_PENDING_FOLDER_DELETION = "delete from folder_pending_deletion where id = ?"
        private const val DELETE_ALL_PENDING_NOTE_DELETIONS = "delete from note_pending_deletion"
        private const val DELETE_ALL_PENDING_FOLDER_DELETIONS = "delete from folder_pending_deletion"

        private val DESCENDING_NOTES = compareByDescending<com.edufelip.shared.db.Note> { it.updated_at }
            .thenByDescending { it.id }
        private val DESCENDING_FOLDERS = compareByDescending<com.edufelip.shared.db.Folder> { it.updated_at }
            .thenByDescending { it.id }
    }
}

private class TestPreparedStatement : SqlPreparedStatement {
    private val longs = mutableMapOf<Int, Long?>()
    private val strings = mutableMapOf<Int, String?>()

    override fun bindBytes(index: Int, bytes: ByteArray?) {}

    override fun bindLong(index: Int, long: Long?) {
        longs[index] = long
    }

    override fun bindDouble(index: Int, double: Double?) {}

    override fun bindString(index: Int, string: String?) {
        strings[index] = string
    }

    override fun bindBoolean(index: Int, boolean: Boolean?) {
        longs[index] = boolean?.let { if (it) 1 else 0 }
    }

    fun long(index: Int): Long? = longs[index]
    fun string(index: Int): String? = strings[index]
}
private class NoteCursor(
    private val rows: List<com.edufelip.shared.db.Note>,
) : SqlCursor {
    private var index = -1

    override fun next(): QueryResult<Boolean> {
        index += 1
        return QueryResult.Value(index < rows.size)
    }

    override fun getString(index: Int): String? = when (index) {
        1 -> current().title
        2 -> current().description
        3 -> current().description_spans
        4 -> current().attachments
        5 -> current().blocks
        6 -> current().content_json
        else -> null
    }

    override fun getLong(index: Int): Long? = when (index) {
        0 -> current().id
        7 -> current().deleted
        8 -> current().created_at
        9 -> current().updated_at
        10 -> current().local_dirty
        11 -> current().local_updated_at
        12 -> current().folder_id
        else -> null
    }

    override fun getBytes(index: Int): ByteArray? = null

    override fun getDouble(index: Int): Double? = null

    override fun getBoolean(index: Int): Boolean? = null

    private fun current(): com.edufelip.shared.db.Note {
        require(index in rows.indices) { "Cursor index out of bounds $index" }
        return rows[index]
    }
}

private class FolderCursor(
    private val rows: List<com.edufelip.shared.db.Folder>,
) : SqlCursor {
    private var index = -1

    override fun next(): QueryResult<Boolean> {
        index += 1
        return QueryResult.Value(index < rows.size)
    }

    override fun getString(index: Int): String? = if (index == 1) current().name else null

    override fun getLong(index: Int): Long? = when (index) {
        0 -> current().id
        2 -> current().created_at
        3 -> current().updated_at
        4 -> current().deleted
        5 -> current().local_dirty
        6 -> current().local_updated_at
        else -> null
    }

    override fun getBytes(index: Int): ByteArray? = null

    override fun getDouble(index: Int): Double? = null

    override fun getBoolean(index: Int): Boolean? = null

    private fun current(): com.edufelip.shared.db.Folder {
        require(index in rows.indices) { "Cursor index out of bounds $index" }
        return rows[index]
    }
}

private class ScalarCursor(private val value: Long) : SqlCursor {
    private var consumed = false
    override fun next(): QueryResult<Boolean> {
        if (consumed) return QueryResult.Value(false)
        consumed = true
        return QueryResult.Value(true)
    }

    override fun getLong(index: Int): Long? = if (index == 0 && consumed) value else null
    override fun getString(index: Int): String? = null
    override fun getBytes(index: Int): ByteArray? = null
    override fun getDouble(index: Int): Double? = null
    override fun getBoolean(index: Int): Boolean? = null
}

private data class PendingDeletionRow(val id: Long, val deletedAt: Long, val stableId: String?, val storagePaths: String)

private class PendingDeletionCursor(
    private val rows: List<PendingDeletionRow>,
) : SqlCursor {
    private var index = -1

    override fun next(): QueryResult<Boolean> {
        index += 1
        return QueryResult.Value(index < rows.size)
    }

    override fun getLong(index: Int): Long? = when (index) {
        0 -> current().id
        1 -> current().deletedAt
        else -> null
    }

    override fun getString(index: Int): String? = when (index) {
        2 -> current().stableId
        3 -> current().storagePaths
        else -> null
    }
    override fun getBytes(index: Int): ByteArray? = null
    override fun getDouble(index: Int): Double? = null
    override fun getBoolean(index: Int): Boolean? = null

    private fun current(): PendingDeletionRow {
        require(index in rows.indices) { "Cursor index out of bounds $index" }
        return rows[index]
    }
}

private fun encodePaths(paths: List<String>): String = storagePathsJson.encodeToString(paths)

private val storagePathsJson = Json { ignoreUnknownKeys = true }

private fun String.normalizeSql(): String = trim()
    .lowercase()
    .removeSuffix(";")
    .replace(Regex("\\s+"), " ")
