package com.edufelip.shared.data.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TestNoteDriver : SqlDriver {
    private data class PendingNoteDeletion(val deletedAt: Long, val stableId: String, val storagePaths: String)
    private val notes = LinkedHashMap<Long, com.edufelip.shared.db.Note>()
    private val folders = LinkedHashMap<Long, com.edufelip.shared.db.Folder>()
    private val pendingNoteDeletions = LinkedHashMap<Long, PendingNoteDeletion>()
    private val pendingFolderDeletions = LinkedHashMap<Long, Long>()
    private val pendingAttachmentDeletions = mutableSetOf<String>()
    private var lastInsertedFolderId: Long = 0L
    private var nextNoteId: Long = 1L
    private var nextFolderId: Long = 1L
    var folderClears: Int = 0

    fun seed(note: com.edufelip.shared.db.Note) {
        notes[note.id] = note
    }

    fun allNotes(): Collection<com.edufelip.shared.db.Note> = notes.values

    fun getNote(id: Long): com.edufelip.shared.db.Note? = notes[id]

    fun requireNote(id: Long): com.edufelip.shared.db.Note = getNote(id) ?: error("Missing note $id. Available: ${notes.keys}")

    fun getFolder(id: Long): com.edufelip.shared.db.Folder? = folders[id]

    override fun <R> executeQuery(
        identifier: Int?,
        sql: String,
        mapper: (SqlCursor) -> QueryResult<R>,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<R> {
        val normalized = sql.normalizeSql()
        println("ExecuteQuery: $normalized")
        val statement = binders?.let { TestPreparedStatement().apply(it) }
        return when {
            normalized.contains(COUNT_DIRTY_NOTES) -> mapper(
                ScalarCursor(notes.values.count { it.local_dirty == 1L }.toLong()),
            )
            normalized.contains(COUNT_DIRTY_FOLDERS) -> mapper(
                ScalarCursor(folders.values.count { it.local_dirty == 1L }.toLong()),
            )
            normalized.contains(COUNT_PENDING_NOTE_DELETIONS) -> mapper(
                ScalarCursor(pendingNoteDeletions.size.toLong()),
            )
            normalized.contains(COUNT_PENDING_FOLDER_DELETIONS) -> mapper(
                ScalarCursor(pendingFolderDeletions.size.toLong()),
            )
            normalized.contains(COUNT_PENDING_ATTACHMENT_DELETIONS) -> mapper(
                ScalarCursor(pendingAttachmentDeletions.size.toLong()),
            )
            normalized.contains(COUNT_NOTES_IN_FOLDER) -> {
                val folderId = statement?.long(0)
                val count = notes.values.count { it.folder_id == folderId }
                mapper(ScalarCursor(count.toLong()))
            }
            normalized.contains(SELECT_PENDING_NOTE_DELETIONS) -> mapper(
                PendingDeletionCursor(
                    pendingNoteDeletions.map { (id, data) -> PendingDeletionRow(id, data.deletedAt, data.stableId, data.storagePaths) },
                ),
            )
            normalized.contains(SELECT_PENDING_FOLDER_DELETIONS) -> mapper(
                PendingDeletionCursor(
                    pendingFolderDeletions.map { (id, deletedAt) -> PendingDeletionRow(id, deletedAt, null, "[]") },
                ),
            )
            normalized.contains(SELECT_PENDING_ATTACHMENT_DELETIONS) -> mapper(
                AttachmentCursor(pendingAttachmentDeletions.sorted()),
            )
            normalized.contains(SELECT_NOTES_ACTIVE) -> mapper(NoteCursor(notes.values.filter { it.deleted == 0L }.sortedWith(DESCENDING_NOTES)))
            normalized.contains(SELECT_NOTES_DELETED) -> mapper(NoteCursor(notes.values.filter { it.deleted == 1L }.sortedWith(DESCENDING_NOTES)))
            normalized.contains(SELECT_NOTES_DIRTY) -> mapper(NoteCursor(notes.values.filter { it.local_dirty == 1L }.sortedWith(DESCENDING_NOTES)))
            normalized.contains(SELECT_NOTE_BY_ID) -> {
                val id = statement?.long(0) ?: 0L
                mapper(NoteCursor(listOfNotNull(notes[id])))
            }
            normalized.contains(SELECT_FOLDERS_ACTIVE) -> mapper(FolderCursor(folders.values.filter { it.deleted == 0L }.sortedWith(DESCENDING_FOLDERS)))
            normalized.contains(SELECT_FOLDERS_DIRTY) -> mapper(FolderCursor(folders.values.filter { it.local_dirty == 1L }.sortedWith(DESCENDING_FOLDERS)))
            normalized.contains(SELECT_FOLDER_BY_ID) -> {
                val id = statement?.long(0) ?: 0L
                mapper(FolderCursor(listOfNotNull(folders[id])))
            }
            normalized.contains(SELECT_FOLDERS_ALL) -> mapper(FolderCursor(folders.values.sortedWith(DESCENDING_FOLDERS)))
            normalized.contains(SELECT_LAST_INSERT_ID) -> mapper(ScalarCursor(lastInsertedFolderId))
            else -> error("Unhandled query: $sql (normalized: $normalized)")
        }
    }

    override fun execute(
        identifier: Int?,
        sql: String,
        parameters: Int,
        binders: (SqlPreparedStatement.() -> Unit)?,
    ): QueryResult<Long> {
        val normalized = sql.normalizeSql()
        println("Execute: $normalized")
        val statement = TestPreparedStatement().apply { binders?.invoke(this) }
        when {
            normalized.contains(INSERT_PENDING_NOTE_DELETION) -> {
                val id = statement.long(0) ?: error("note id required")
                val deletedAt = statement.long(1) ?: 0L
                val stableId = statement.string(2) ?: id.toString()
                val storagePaths = statement.string(3) ?: "[]"
                pendingNoteDeletions[id] = PendingNoteDeletion(deletedAt, stableId, storagePaths)
            }

            normalized.contains(DELETE_PENDING_NOTE_DELETION) -> {
                val id = statement.long(0) ?: return QueryResult.Value(0)
                pendingNoteDeletions.remove(id)
            }

            normalized.contains(INSERT_PENDING_FOLDER_DELETION) -> {
                val id = statement.long(0) ?: error("folder id required")
                val deletedAt = statement.long(1) ?: 0L
                pendingFolderDeletions[id] = deletedAt
            }

            normalized.contains(DELETE_PENDING_FOLDER_DELETION) -> {
                val id = statement.long(0) ?: return QueryResult.Value(0)
                pendingFolderDeletions.remove(id)
            }

            normalized.contains(INSERT_PENDING_ATTACHMENT_DELETION) -> {
                val path = statement.string(0) ?: error("path required")
                pendingAttachmentDeletions.add(path)
            }

            normalized.contains(DELETE_PENDING_ATTACHMENT_DELETION) -> {
                val path = statement.string(0) ?: return QueryResult.Value(0)
                pendingAttachmentDeletions.remove(path)
            }

            normalized.contains(DELETE_ALL_PENDING_NOTE_DELETIONS) -> pendingNoteDeletions.clear()
            normalized.contains(DELETE_ALL_PENDING_FOLDER_DELETIONS) -> pendingFolderDeletions.clear()
            normalized.contains(DELETE_ALL_PENDING_ATTACHMENT_DELETIONS) -> pendingAttachmentDeletions.clear()

            normalized.contains(INSERT_NOTE_WITH_ID) -> {
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

            normalized.contains(INSERT_NOTE) -> {
                val id = nextNoteId++
                notes[id] = com.edufelip.shared.db.Note(
                    id = id,
                    title = statement.string(0).orEmpty(),
                    description = statement.string(1).orEmpty(),
                    description_spans = statement.string(2).orEmpty(),
                    attachments = statement.string(3).orEmpty(),
                    blocks = statement.string(4).orEmpty(),
                    content_json = statement.string(5),
                    deleted = 0,
                    created_at = statement.long(6) ?: 0,
                    updated_at = statement.long(7) ?: 0,
                    local_dirty = 1,
                    local_updated_at = statement.long(8) ?: 0,
                    folder_id = statement.long(9),
                    stable_id = statement.string(10).orEmpty(),
                )
            }

            normalized.contains(UPDATE_NOTE_FROM_REMOTE) -> {
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

            normalized.contains(SET_DELETED) -> {
                val id = statement.long(3) ?: error("id required")
                val existing = notes[id] ?: return QueryResult.Value(0)
                notes[id] = existing.copy(
                    deleted = statement.long(0) ?: 0,
                    updated_at = statement.long(1) ?: existing.updated_at,
                    local_dirty = 1,
                    local_updated_at = statement.long(2) ?: 0,
                )
            }

            normalized.contains(UPDATE_NOTE) -> {
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
                    local_dirty = 1,
                    local_updated_at = statement.long(8) ?: 0,
                    folder_id = statement.long(9),
                )
            }

            normalized.contains(UPDATE_CACHED_CONTENT) -> {
                val id = statement.long(2) ?: error("id required")
                val existing = notes[id] ?: return QueryResult.Value(0)
                notes[id] = existing.copy(
                    content_json = statement.string(0),
                    local_updated_at = statement.long(1) ?: 0,
                )
            }

            normalized.contains(SET_NOTE_FOLDER) -> {
                val id = statement.long(3) ?: error("id required")
                val existing = notes[id] ?: return QueryResult.Value(0)
                notes[id] = existing.copy(
                    folder_id = statement.long(0),
                    updated_at = statement.long(1) ?: existing.updated_at,
                    local_dirty = 1,
                    local_updated_at = statement.long(2) ?: 0,
                )
            }

            normalized.contains(CLEAR_FOLDER_ASSIGNMENT) -> {
                val folderId = statement.long(2) ?: error("folder_id required")
                notes.forEach { (id, note) ->
                    if (note.folder_id == folderId) {
                        notes[id] = note.copy(
                            folder_id = null,
                            updated_at = statement.long(0) ?: note.updated_at,
                            local_dirty = 1,
                            local_updated_at = statement.long(1) ?: 0,
                        )
                    }
                }
            }

            normalized.contains(DELETE_NOTE_BY_ID) -> {
                val id = statement.long(0) ?: return QueryResult.Value(0)
                notes.remove(id)
            }

            normalized.contains(DELETE_ALL_NOTES) -> notes.clear()
            normalized.contains(CLEAR_NOTE_DIRTY) -> {
                val id = statement.long(0) ?: return QueryResult.Value(0)
                notes[id]?.let { notes[id] = it.copy(local_dirty = 0) }
            }

            normalized.contains(DELETE_ALL_FOLDERS) -> {
                folders.clear()
                folderClears += 1
            }

            normalized.contains(INSERT_FOLDER_WITH_ID) -> {
                val id = statement.long(0) ?: 0L
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

            normalized.contains(INSERT_FOLDER) -> {
                val id = nextFolderId++
                val row = com.edufelip.shared.db.Folder(
                    id = id,
                    name = statement.string(0).orEmpty(),
                    created_at = statement.long(1) ?: 0,
                    updated_at = statement.long(2) ?: 0,
                    deleted = 0,
                    local_dirty = 1,
                    local_updated_at = statement.long(3) ?: 0,
                )
                folders[id] = row
                lastInsertedFolderId = id
            }

            normalized.contains(UPDATE_FOLDER_FROM_REMOTE) -> {
                val id = statement.long(3) ?: error("folder id required")
                val existing = folders[id] ?: return QueryResult.Value(0)
                folders[id] = existing.copy(
                    name = statement.string(0).orEmpty(),
                    updated_at = statement.long(1) ?: existing.updated_at,
                    deleted = statement.long(2) ?: existing.deleted,
                    local_dirty = 0,
                )
            }

            normalized.contains(UPDATE_FOLDER) -> {
                val id = statement.long(4) ?: error("folder id required")
                val existing = folders[id] ?: return QueryResult.Value(0)
                folders[id] = existing.copy(
                    name = statement.string(0).orEmpty(),
                    updated_at = statement.long(1) ?: existing.updated_at,
                    local_dirty = 1,
                    local_updated_at = statement.long(2) ?: 0,
                )
            }

            normalized.contains(DELETE_FOLDER_BY_ID) -> {
                val id = statement.long(0) ?: return QueryResult.Value(0)
                folders.remove(id)
            }

            normalized.contains(DELETE_FOLDER) -> {
                val id = statement.long(0) ?: return QueryResult.Value(0)
                folders.remove(id)
            }

            normalized.contains(CLEAR_FOLDER_DIRTY) -> {
                val id = statement.long(0) ?: return QueryResult.Value(0)
                folders[id]?.let { folders[id] = it.copy(local_dirty = 0) }
            }

            normalized.contains(MARK_FOLDER_DELETED) -> {
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

            else -> error("Unhandled statement: $sql (normalized: $normalized)")
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
        private const val SELECT_NOTES_ACTIVE = "FROM NOTE WHERE DELETED = 0"
        private const val SELECT_NOTES_DELETED = "FROM NOTE WHERE DELETED = 1"
        private const val SELECT_NOTES_DIRTY = "FROM NOTE WHERE LOCAL_DIRTY = 1"
        private const val COUNT_DIRTY_NOTES = "COUNT(*) FROM NOTE WHERE LOCAL_DIRTY = 1"
        private const val SELECT_NOTE_BY_ID = "FROM NOTE WHERE ID = ?"
        private const val SELECT_FOLDERS_ACTIVE = "FROM FOLDER WHERE DELETED = 0"
        private const val SELECT_FOLDERS_ALL = "FROM FOLDER ORDER BY UPDATED_AT DESC, ID DESC"
        private const val SELECT_FOLDERS_DIRTY = "FROM FOLDER WHERE LOCAL_DIRTY = 1"
        private const val COUNT_DIRTY_FOLDERS = "COUNT(*) FROM FOLDER WHERE LOCAL_DIRTY = 1"
        private const val SELECT_FOLDER_BY_ID = "FROM FOLDER WHERE ID = ?"
        private const val SELECT_LAST_INSERT_ID = "LAST_INSERT_ROWID()"
        private const val INSERT_NOTE = "INSERT INTO NOTE(TITLE, DESCRIPTION, DESCRIPTION_SPANS, ATTACHMENTS, BLOCKS, CONTENT_JSON, DELETED, CREATED_AT, UPDATED_AT, LOCAL_DIRTY, LOCAL_UPDATED_AT, FOLDER_ID, STABLE_ID)"
        private const val INSERT_NOTE_WITH_ID = "INTO NOTE(ID, TITLE, DESCRIPTION, DESCRIPTION_SPANS, ATTACHMENTS, BLOCKS, CONTENT_JSON, DELETED, CREATED_AT, UPDATED_AT, FOLDER_ID, STABLE_ID)"
        private const val UPDATE_NOTE = "UPDATE NOTE SET TITLE = ?, DESCRIPTION = ?, DESCRIPTION_SPANS = ?, ATTACHMENTS = ?, BLOCKS = ?, CONTENT_JSON = ?, DELETED = ?, UPDATED_AT = ?, LOCAL_DIRTY = 1, LOCAL_UPDATED_AT = ?, FOLDER_ID = ?"
        private const val UPDATE_CACHED_CONTENT = "UPDATE NOTE SET CONTENT_JSON = ?, LOCAL_UPDATED_AT = ? WHERE ID = ?"
        private const val UPDATE_NOTE_FROM_REMOTE = "UPDATE NOTE SET TITLE = ?, DESCRIPTION = ?, DESCRIPTION_SPANS = ?, ATTACHMENTS = ?, BLOCKS = ?, CONTENT_JSON = ?, DELETED = ?, UPDATED_AT = ?, LOCAL_DIRTY = 0"
        private const val SET_DELETED = "UPDATE NOTE SET DELETED = ?, UPDATED_AT = ?, LOCAL_DIRTY = 1, LOCAL_UPDATED_AT = ? WHERE ID = ?"
        private const val DELETE_NOTE_BY_ID = "DELETE FROM NOTE WHERE ID = ?"
        private const val DELETE_ALL_NOTES = "DELETE FROM NOTE"
        private const val CLEAR_NOTE_DIRTY = "UPDATE NOTE SET LOCAL_DIRTY = 0 WHERE ID = ?"
        private const val DELETE_ALL_FOLDERS = "DELETE FROM FOLDER"
        private const val INSERT_FOLDER = "INSERT INTO FOLDER(NAME, CREATED_AT, UPDATED_AT, DELETED, LOCAL_DIRTY, LOCAL_UPDATED_AT)"
        private const val INSERT_FOLDER_WITH_ID = "INTO FOLDER(ID, NAME, CREATED_AT, UPDATED_AT, DELETED, LOCAL_DIRTY, LOCAL_UPDATED_AT)"
        private const val UPDATE_FOLDER = "UPDATE FOLDER SET NAME = ?, UPDATED_AT = ?, LOCAL_DIRTY = 1, LOCAL_UPDATED_AT = ?"
        private const val UPDATE_FOLDER_FROM_REMOTE = "UPDATE FOLDER SET NAME = ?, UPDATED_AT = ?, DELETED = ?, LOCAL_DIRTY = 0"
        private const val DELETE_FOLDER_BY_ID = "DELETE FROM FOLDER WHERE ID = ?"
        private const val DELETE_FOLDER = "DELETE FROM FOLDER WHERE ID = ?"
        private const val CLEAR_FOLDER_DIRTY = "UPDATE FOLDER SET LOCAL_DIRTY = 0 WHERE ID = ?"
        private const val MARK_FOLDER_DELETED = "UPDATE FOLDER SET DELETED = 1"
        private const val SET_NOTE_FOLDER = "UPDATE NOTE SET FOLDER_ID = ?, UPDATED_AT = ?, LOCAL_DIRTY = 1, LOCAL_UPDATED_AT = ?"
        private const val CLEAR_FOLDER_ASSIGNMENT = "UPDATE NOTE SET FOLDER_ID = NULL, UPDATED_AT = ?, LOCAL_DIRTY = 1, LOCAL_UPDATED_AT = ? WHERE FOLDER_ID = ?"
        private const val SELECT_PENDING_NOTE_DELETIONS = "FROM NOTE_PENDING_DELETION"
        private const val COUNT_PENDING_NOTE_DELETIONS = "COUNT(*) FROM NOTE_PENDING_DELETION"
        private const val SELECT_PENDING_FOLDER_DELETIONS = "FROM FOLDER_PENDING_DELETION"
        private const val COUNT_PENDING_FOLDER_DELETIONS = "COUNT(*) FROM FOLDER_PENDING_DELETION"
        private const val INSERT_PENDING_NOTE_DELETION = "INTO NOTE_PENDING_DELETION(ID, DELETED_AT, STABLE_ID, STORAGE_PATHS)"
        private const val DELETE_PENDING_NOTE_DELETION = "DELETE FROM NOTE_PENDING_DELETION WHERE ID = ?"
        private const val INSERT_PENDING_FOLDER_DELETION = "INTO FOLDER_PENDING_DELETION(ID, DELETED_AT)"
        private const val DELETE_PENDING_FOLDER_DELETION = "DELETE FROM FOLDER_PENDING_DELETION WHERE ID = ?"
        private const val DELETE_ALL_PENDING_NOTE_DELETIONS = "DELETE FROM NOTE_PENDING_DELETION"
        private const val DELETE_ALL_PENDING_FOLDER_DELETIONS = "DELETE FROM FOLDER_PENDING_DELETION"
        private const val SELECT_PENDING_ATTACHMENT_DELETIONS = "FROM ATTACHMENT_PENDING_DELETION"
        private const val COUNT_PENDING_ATTACHMENT_DELETIONS = "COUNT(*) FROM ATTACHMENT_PENDING_DELETION"
        private const val DELETE_PENDING_ATTACHMENT_DELETION = "DELETE FROM ATTACHMENT_PENDING_DELETION WHERE PATH = ?"
        private const val INSERT_PENDING_ATTACHMENT_DELETION = "INTO ATTACHMENT_PENDING_DELETION(PATH)"
        private const val DELETE_ALL_PENDING_ATTACHMENT_DELETIONS = "DELETE FROM ATTACHMENT_PENDING_DELETION"
        private const val COUNT_NOTES_IN_FOLDER = "COUNT(*) FROM NOTE WHERE FOLDER_ID = ?"

        private val DESCENDING_NOTES = compareByDescending<com.edufelip.shared.db.Note> { it.updated_at }
            .thenByDescending { it.id }
        private val DESCENDING_FOLDERS = compareByDescending<com.edufelip.shared.db.Folder> { it.updated_at }
            .thenByDescending { it.id }
    }
}

class TestPreparedStatement : SqlPreparedStatement {
    private val longs = mutableMapOf<Int, Long?>()
    private val strings = mutableMapOf<Int, String?>()

    override fun bindBytes(index: Int, bytes: ByteArray?) {}

    override fun bindLong(index: Int, long: Long?) {
        println("BindLong: $index = $long")
        longs[index] = long
    }

    override fun bindDouble(index: Int, double: Double?) {}

    override fun bindString(index: Int, string: String?) {
        println("BindString: $index = $string")
        strings[index] = string
    }

    override fun bindBoolean(index: Int, boolean: Boolean?) {
        val value = boolean?.let { if (it) 1L else 0L }
        println("BindBoolean: $index = $value")
        longs[index] = value
    }

    fun long(index: Int): Long? = longs[index]
    fun string(index: Int): String? = strings[index]
}

class NoteCursor(
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
        13 -> current().stable_id
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

class FolderCursor(
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

class ScalarCursor(private val value: Long) : SqlCursor {
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

data class PendingDeletionRow(val id: Long, val deletedAt: Long, val stableId: String?, val storagePaths: String)

class PendingDeletionCursor(
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

class AttachmentCursor(
    private val paths: List<String>,
) : SqlCursor {
    private var index = -1

    override fun next(): QueryResult<Boolean> {
        index += 1
        return QueryResult.Value(index < paths.size)
    }

    override fun getString(index: Int): String? = if (index == 0) paths[index] else null
    override fun getLong(index: Int): Long? = null
    override fun getBytes(index: Int): ByteArray? = null
    override fun getDouble(index: Int): Double? = null
    override fun getBoolean(index: Int): Boolean? = null
}

class ScalarStringCursor(private val value: String?) : SqlCursor {
    private var consumed = false
    override fun next(): QueryResult<Boolean> {
        if (consumed) return QueryResult.Value(false)
        consumed = true
        return QueryResult.Value(true)
    }

    override fun getString(index: Int): String? = if (index == 0 && consumed) value else null
    override fun getLong(index: Int): Long? = null
    override fun getBytes(index: Int): ByteArray? = null
    override fun getDouble(index: Int): Double? = null
    override fun getBoolean(index: Int): Boolean? = null
}

fun String.normalizeSql(): String = this
    .replace("\r", " ")
    .replace("\n", " ")
    .replace(Regex("\\s+"), " ")
    .replace(" (", "(")
    .trim()
    .uppercase()

fun encodePaths(paths: List<String>): String = storagePathsJson.encodeToString(paths)

private val storagePathsJson = Json { ignoreUnknownKeys = true }
