package com.edufelip.shared.data.repository

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.edufelip.shared.core.time.nowEpochMs
import com.edufelip.shared.data.db.decryptField
import com.edufelip.shared.data.db.encryptField
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.NoteAttachment
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.NoteTextSpan
import com.edufelip.shared.domain.model.attachmentsFromJson
import com.edufelip.shared.domain.model.generateStableNoteId
import com.edufelip.shared.domain.model.noteContentFromJson
import com.edufelip.shared.domain.model.spansFromJson
import com.edufelip.shared.domain.model.toJson
import com.edufelip.shared.domain.model.toSummary
import com.edufelip.shared.domain.model.withFallbacks
import com.edufelip.shared.domain.repository.NoteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SqlDelightNoteRepository(
    private val database: NoteDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : NoteRepository {

    private val queries get() = database.noteQueries

    private fun currentTimeMillis(): Long = nowEpochMs()

    private fun mapRowToNote(row: com.edufelip.shared.db.Note): Note {
        val title = decryptField(row.title)
        val description = decryptField(row.description)
        val spansJson = decryptField(row.description_spans)
        val attachmentsJson = decryptField(row.attachments)
        val contentJson = row.content_json?.let(::decryptField)
        val spans = spansFromJson(spansJson)
        val attachments = attachmentsFromJson(attachmentsJson)
        val content = noteContentFromJson(contentJson)
        val summary = content.toSummary().withFallbacks(description, spans, attachments)
        val stableId = row.stable_id.takeIf { it.isNotBlank() } ?: row.id.toString()
        return Note(
            id = row.id.toInt(),
            stableId = stableId,
            title = title,
            description = summary.description,
            deleted = row.deleted != 0L,
            createdAt = row.created_at,
            updatedAt = row.updated_at,
            dirty = row.local_dirty != 0L,
            localUpdatedAt = row.local_updated_at,
            folderId = row.folder_id,
            descriptionSpans = summary.spans,
            attachments = summary.attachments,
            content = content,
        )
    }

    private fun mapRowToFolder(row: com.edufelip.shared.db.Folder): Folder = Folder(
        id = row.id,
        name = row.name,
        createdAt = row.created_at,
        updatedAt = row.updated_at,
        deleted = row.deleted != 0L,
        dirty = row.local_dirty != 0L,
        localUpdatedAt = row.local_updated_at,
    )

    override fun notes(): Flow<List<Note>> = queries.selectAll().asFlow().mapToList(dispatcher).map { rows -> rows.map(::mapRowToNote) }

    override fun trash(): Flow<List<Note>> = queries.selectDeleted().asFlow().mapToList(dispatcher).map { rows -> rows.map(::mapRowToNote) }

    override fun notesByFolder(folderId: Long): Flow<List<Note>> = queries.selectNotesByFolder(folder_id = folderId).asFlow().mapToList(dispatcher).map { rows -> rows.map(::mapRowToNote) }

    override fun notesWithoutFolder(): Flow<List<Note>> = queries.selectNotesWithoutFolder().asFlow().mapToList(dispatcher).map { rows -> rows.map(::mapRowToNote) }

    override fun folders(): Flow<List<Folder>> = queries.selectFolders().asFlow().mapToList(dispatcher).map { rows -> rows.map(::mapRowToFolder) }

    override suspend fun insert(
        title: String,
        description: String,
        folderId: Long?,
        spans: List<NoteTextSpan>,
        attachments: List<NoteAttachment>,
        content: NoteContent,
        stableId: String?,
    ) {
        val finalContent = if (content.blocks.isEmpty()) NoteContent() else content
        val summary = finalContent.toSummary()
        val normalizedDescription = summary.description.ifBlank { description }
        val normalizedSpans = summary.spans.ifEmpty { spans }
        val normalizedAttachments = if (summary.attachments.isNotEmpty()) summary.attachments else attachments
        val now = currentTimeMillis()
        val resolvedStableId = stableId?.takeIf { it.isNotBlank() } ?: generateStableNoteId()
        queries.insertNote(
            title = encryptField(title),
            description = encryptField(normalizedDescription),
            description_spans = encryptField(normalizedSpans.toJson()),
            attachments = encryptField(normalizedAttachments.toJson()),
            blocks = "[]",
            content_json = encryptField(finalContent.toJson()),
            created_at = now,
            updated_at = now,
            local_updated_at = now,
            folder_id = folderId,
            stable_id = resolvedStableId,
        )
    }

    override suspend fun update(
        id: Int,
        title: String,
        description: String,
        deleted: Boolean,
        folderId: Long?,
        spans: List<NoteTextSpan>,
        attachments: List<NoteAttachment>,
        content: NoteContent,
    ) {
        val finalContent = if (content.blocks.isEmpty()) NoteContent() else content
        val summary = finalContent.toSummary()
        val normalizedDescription = summary.description.ifBlank { description }
        val normalizedSpans = summary.spans.ifEmpty { spans }
        val normalizedAttachments = if (summary.attachments.isNotEmpty()) summary.attachments else attachments
        val now = currentTimeMillis()
        queries.updateNote(
            title = encryptField(title),
            description = encryptField(normalizedDescription),
            description_spans = encryptField(normalizedSpans.toJson()),
            attachments = encryptField(normalizedAttachments.toJson()),
            blocks = "[]",
            content_json = encryptField(finalContent.toJson()),
            deleted = if (deleted) 1 else 0,
            updated_at = now,
            local_updated_at = now,
            folder_id = folderId,
            id = id.toLong(),
        )
    }

    override suspend fun setDeleted(id: Int, deleted: Boolean) {
        val now = currentTimeMillis()
        queries.setDeleted(
            deleted = if (deleted) 1 else 0,
            updated_at = now,
            local_updated_at = now,
            id = id.toLong(),
        )
    }

    override suspend fun delete(id: Int) {
        val now = currentTimeMillis()
        val row = queries.selectById(id.toLong()).executeAsOneOrNullCompat() ?: return
        val stableId = row.stable_id.takeIf { it.isNotBlank() } ?: row.id.toString()
        val contentJson = row.content_json?.let(::decryptField)
        val content = noteContentFromJson(contentJson)
        val storagePaths = content.blocks
            .filterIsInstance<ImageBlock>()
            .mapNotNull { it.storagePath?.takeIf { path -> path.isNotBlank() } }
        queries.insertPendingNoteDeletion(
            id = row.id,
            deleted_at = now,
            stable_id = stableId,
            storage_paths = encodeStoragePaths(storagePaths),
        )
        queries.deleteById(row.id)
    }

    override suspend fun assignToFolder(id: Int, folderId: Long?) {
        val now = currentTimeMillis()
        queries.setNoteFolder(
            folder_id = folderId,
            updated_at = now,
            local_updated_at = now,
            id = id.toLong(),
        )
    }

    override suspend fun insertFolder(name: String): Long {
        val now = currentTimeMillis()
        queries.insertFolder(
            name = name,
            created_at = now,
            updated_at = now,
            local_updated_at = now,
        )
        return queries.lastInsertedFolderId().executeAsOne()
    }

    override suspend fun renameFolder(id: Long, name: String) {
        val now = currentTimeMillis()
        queries.updateFolder(
            name = name,
            updated_at = now,
            local_updated_at = now,
            id = id,
        )
    }

    override suspend fun deleteFolder(id: Long) {
        val now = currentTimeMillis()
        queries.clearFolderAssignment(
            updated_at = now,
            local_updated_at = now,
            folder_id = id,
        )
        queries.insertPendingFolderDeletion(
            id = id,
            deleted_at = now,
        )
        queries.deleteFolder(id)
    }
}

private fun <T : Any> Query<T>.executeAsOneOrNullCompat(): T? = try {
    executeAsOne()
} catch (_: IllegalStateException) {
    null
}

private fun encodeStoragePaths(paths: List<String>): String = storagePathsJson.encodeToString(paths)

private val storagePathsJson = Json { ignoreUnknownKeys = true }
