package com.edufelip.shared.data.cloud

import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface CloudNotesDataSource {
    fun observe(uid: String): Flow<RemoteSyncPayload>
    suspend fun getAll(uid: String): RemoteSyncPayload
    suspend fun upsert(uid: String, note: Note)
    suspend fun delete(uid: String, id: Int)
    suspend fun upsertPreserveUpdatedAt(uid: String, note: Note)
    suspend fun upsertFolder(uid: String, folder: Folder)
    suspend fun deleteFolder(uid: String, id: Long)
}

data class RemoteSyncPayload(
    val notes: List<Note>,
    val folders: List<Folder>,
)

fun provideCloudNotesDataSource(): CloudNotesDataSource = GitLiveCloudNotesDataSource

interface CurrentUserProvider {
    val uid: Flow<String?>
}

fun provideCurrentUserProvider(): CurrentUserProvider = GitLiveCurrentUserProvider
