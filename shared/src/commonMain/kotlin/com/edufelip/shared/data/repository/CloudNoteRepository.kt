package com.edufelip.shared.data.repository

import com.edufelip.shared.core.time.nowEpochMs
import com.edufelip.shared.data.cloud.CloudNotesDataSource
import com.edufelip.shared.data.cloud.CurrentUserProvider
import com.edufelip.shared.data.cloud.provideCloudNotesDataSource
import com.edufelip.shared.data.cloud.provideCurrentUserProvider
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.domain.model.NoteAttachment
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.domain.model.NoteTextSpan
import com.edufelip.shared.domain.model.generateStableNoteId
import com.edufelip.shared.domain.model.toSummary
import com.edufelip.shared.domain.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class CloudNoteRepository(
    private val cloud: CloudNotesDataSource = provideCloudNotesDataSource(),
    private val currentUser: CurrentUserProvider = provideCurrentUserProvider(),
) : NoteRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun notes(): Flow<List<Note>> = currentUser.uid.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList()) else cloud.observe(uid).map { it.notes }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun trash(): Flow<List<Note>> = notes().flatMapLatest { list -> flowOf(list.filter { it.deleted }) }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun notesByFolder(folderId: Long): Flow<List<Note>> = notes().flatMapLatest { list -> flowOf(list.filter { it.folderId == folderId }) }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun notesWithoutFolder(): Flow<List<Note>> = notes().flatMapLatest { list -> flowOf(list.filter { it.folderId == null }) }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun folders(): Flow<List<Folder>> = currentUser.uid.flatMapLatest { uid ->
        if (uid == null) flowOf(emptyList()) else cloud.observe(uid).map { payload -> payload.folders.filter { !it.deleted } }
    }

    override suspend fun insert(
        title: String,
        description: String,
        folderId: Long?,
        spans: List<NoteTextSpan>,
        attachments: List<NoteAttachment>,
        content: NoteContent,
        stableId: String?,
    ) {
        val uid = currentUser.uid.first() ?: return
        val now = nowEpochMs()
        val finalContent = if (content.blocks.isEmpty()) NoteContent() else content
        val summary = finalContent.toSummary()
        val normalizedDescription = summary.description.ifBlank { description }
        val normalizedSpans = summary.spans.ifEmpty { spans }
        val normalizedAttachments = if (summary.attachments.isNotEmpty()) summary.attachments else attachments
        val resolvedStableId = stableId ?: generateStableNoteId()
        val note = Note(
            id = now.hashCode(),
            stableId = resolvedStableId,
            title = title,
            description = normalizedDescription,
            deleted = false,
            createdAt = now,
            updatedAt = now,
            dirty = false,
            localUpdatedAt = now,
            folderId = folderId,
            descriptionSpans = normalizedSpans,
            attachments = normalizedAttachments,
            content = finalContent,
        )
        cloud.upsert(uid, note)
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
        val uid = currentUser.uid.first() ?: return
        val now = nowEpochMs()
        val finalContent = if (content.blocks.isEmpty()) NoteContent() else content
        val summary = finalContent.toSummary()
        val normalizedDescription = summary.description.ifBlank { description }
        val normalizedSpans = summary.spans.ifEmpty { spans }
        val normalizedAttachments = if (summary.attachments.isNotEmpty()) summary.attachments else attachments
        val note = Note(
            id = id,
            stableId = id.toString(),
            title = title,
            description = normalizedDescription,
            deleted = deleted,
            createdAt = now,
            updatedAt = now,
            dirty = true,
            localUpdatedAt = now,
            folderId = folderId,
            descriptionSpans = normalizedSpans,
            attachments = normalizedAttachments,
            content = finalContent,
        )
        cloud.upsert(uid, note)
    }

    override suspend fun setDeleted(id: Int, deleted: Boolean) {
        val uid = currentUser.uid.first() ?: return
        val now = nowEpochMs()
        cloud.upsert(
            uid,
            Note(
                id = id,
                stableId = id.toString(),
                title = "",
                description = "",
                deleted = deleted,
                createdAt = now,
                updatedAt = now,
                dirty = false,
                localUpdatedAt = now,
                folderId = null,
                descriptionSpans = emptyList(),
                attachments = emptyList(),
                content = NoteContent(),
            ),
        )
    }

    override suspend fun delete(id: Int) {
        val uid = currentUser.uid.first() ?: return
        cloud.delete(uid, id)
    }

    override suspend fun assignToFolder(id: Int, folderId: Long?) {
        // Cloud-only repository is primarily used in previews; caller should invoke update()
        // with the full note contents instead of relying on this no-op.
    }

    override suspend fun insertFolder(name: String): Long {
        val uid = currentUser.uid.first() ?: return 0L
        val now = nowEpochMs()
        val folder = Folder(
            id = now,
            name = name,
            createdAt = now,
            updatedAt = now,
        )
        cloud.upsertFolder(uid, folder)
        return folder.id
    }

    override suspend fun renameFolder(id: Long, name: String) {
        val uid = currentUser.uid.first() ?: return
        val now = nowEpochMs()
        val folder = Folder(
            id = id,
            name = name,
            createdAt = now,
            updatedAt = now,
        )
        cloud.upsertFolder(uid, folder)
    }

    override suspend fun deleteFolder(id: Long) {
        val uid = currentUser.uid.first() ?: return
        cloud.deleteFolder(uid, id)
    }
}
