package com.edufelip.shared.cloud

import com.edufelip.shared.model.Note
import kotlinx.coroutines.flow.Flow

interface CloudNotesDataSource {
    fun observe(uid: String): Flow<List<Note>>
    suspend fun getAll(uid: String): List<Note>
    suspend fun upsert(uid: String, note: Note)
    suspend fun delete(uid: String, id: Int)

    // Upsert that preserves provided updatedAt (no server timestamp). Useful for push-only sync to avoid reordering.
    suspend fun upsertPreserveUpdatedAt(uid: String, note: Note)
}

expect fun provideCloudNotesDataSource(): CloudNotesDataSource

interface CurrentUserProvider {
    val uid: Flow<String?>
}

expect fun provideCurrentUserProvider(): CurrentUserProvider
