package com.edufelip.shared.data.sync

import app.cash.sqldelight.Query
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import app.cash.sqldelight.Transacter
import com.edufelip.shared.data.cloud.CloudNotesDataSource
import com.edufelip.shared.data.cloud.CurrentUserProvider
import com.edufelip.shared.data.db.decryptField
import com.edufelip.shared.data.db.encryptField
import com.edufelip.shared.db.NoteDatabase
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
    ): NotesSyncManager {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = CoroutineScope(SupervisorJob() + dispatcher)
        activeScopes += scope
        return NotesSyncManager(db, scope, cloud, users)
    }

    @Test
    fun syncUploadsDirtyLocalNotesAndClearsDirtyFlag() = runTest {
        NoteCipher.overrideKeyForTests(TEST_KEY)
        val driver = TestNoteDriver()
        val db = NoteDatabase(driver)
        val cloud = FakeCloudNotesDataSource()
        val users = FakeCurrentUserProvider()
        val syncManager = createSyncManager(db, cloud, users)
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
    )
}

private class FakeCloudNotesDataSource : CloudNotesDataSource {
    private val remoteByUser = mutableMapOf<String, MutableMap<Int, Note>>()
    private val flows = mutableMapOf<String, MutableStateFlow<List<Note>>>()

    val upsertCalls = mutableListOf<Pair<String, Note>>()

    override fun observe(uid: String): Flow<List<Note>> =
        flows.getOrPut(uid) { MutableStateFlow(remoteByUser[uid]?.values?.sortedBy { it.updatedAt } ?: emptyList()) }

    override suspend fun getAll(uid: String): List<Note> =
        remoteByUser[uid]?.values?.sortedBy { it.updatedAt } ?: emptyList()

    override suspend fun upsert(uid: String, note: Note) {
        val userNotes = remoteByUser.getOrPut(uid) { mutableMapOf() }
        val serverNote = note.copy(updatedAt = note.updatedAt + 5, dirty = false)
        userNotes[note.id] = serverNote
        upsertCalls += uid to serverNote
        pushRemote(uid)
    }

    override suspend fun delete(uid: String, id: Int) {
        remoteByUser[uid]?.remove(id)
        pushRemote(uid)
    }

    override suspend fun upsertPreserveUpdatedAt(uid: String, note: Note) {
        val userNotes = remoteByUser.getOrPut(uid) { mutableMapOf() }
        userNotes[note.id] = note.copy(dirty = false)
        pushRemote(uid)
    }

    fun seedRemote(uid: String, notes: List<Note>) {
        remoteByUser[uid] = notes.associateBy { it.id }.toMutableMap()
        pushRemote(uid)
    }

    fun pushRemote(uid: String) {
        val state = flows.getOrPut(uid) { MutableStateFlow(emptyList()) }
        val ordered = remoteByUser[uid]?.values?.sortedBy { it.updatedAt } ?: emptyList()
        state.value = ordered
    }
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

private class TestNoteDriver : SqlDriver {
    private val notes = linkedMapOf<Long, com.edufelip.shared.db.Note>()
    var folderClears: Int = 0

    fun seed(note: com.edufelip.shared.db.Note) {
        notes[note.id] = note
    }

    fun allNotes(): Collection<com.edufelip.shared.db.Note> = notes.values

    fun getNote(id: Long): com.edufelip.shared.db.Note? = notes[id]

    fun requireNote(id: Long): com.edufelip.shared.db.Note = getNote(id) ?: error("Missing note $id")

    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (SqlCursor) -> QueryResult<R>,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<R> {
        val rows = when (identifier) {
            SELECT_ALL -> notes.values.filter { it.deleted == 0L }.sortedWith(DESCENDING_BY_UPDATED)
            SELECT_DELETED -> notes.values.filter { it.deleted == 1L }.sortedWith(DESCENDING_BY_UPDATED)
            SELECT_DIRTY -> notes.values.filter { it.local_dirty == 1L }.sortedWith(DESCENDING_BY_UPDATED)
            else -> error("Unhandled query id $identifier ($sql)")
        }
        return mapper(TestCursor(rows))
    }

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<Long> {
        val statement = TestPreparedStatement().apply { binders?.invoke(this) }
        when (identifier) {
            INSERT_WITH_ID -> {
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
                )
            }

            UPDATE_FROM_REMOTE -> {
                val id = statement.long(9) ?: error("id required")
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
                )
            }

            DELETE_BY_ID -> {
                val id = statement.long(0) ?: return QueryResult.Value(0)
                notes.remove(id)
            }

            DELETE_ALL -> notes.clear()
            CLEAR_DIRTY_BY_ID -> {
                val id = statement.long(0) ?: return QueryResult.Value(0)
                notes[id]?.let { existing ->
                    notes[id] = existing.copy(local_dirty = 0)
                }
            }

            DELETE_ALL_FOLDERS -> folderClears += 1
            else -> error("Unhandled execute id $identifier ($sql)")
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
        private const val SELECT_ALL = -284_331_761
        private const val SELECT_DELETED = 1_823_522_951
        private const val SELECT_DIRTY = 1_637_775_296
        private const val INSERT_WITH_ID = -1_511_336_368
        private const val UPDATE_FROM_REMOTE = 565_439_727
        private const val DELETE_BY_ID = -1_098_730_669
        private const val DELETE_ALL = 1_072_934_400
        private const val CLEAR_DIRTY_BY_ID = 1_292_962_605
        private const val DELETE_ALL_FOLDERS = -1_079_272_539

        private val DESCENDING_BY_UPDATED = compareByDescending<com.edufelip.shared.db.Note> { it.updated_at }
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

private class TestCursor(
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
