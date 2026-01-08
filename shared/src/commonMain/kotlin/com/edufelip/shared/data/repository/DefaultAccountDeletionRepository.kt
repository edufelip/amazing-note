package com.edufelip.shared.data.repository

import com.edufelip.shared.data.auth.AuthService
import com.edufelip.shared.data.db.decryptField
import com.edufelip.shared.data.network.AccountDeletionService
import com.edufelip.shared.data.network.provideAccountDeletionService
import com.edufelip.shared.db.NoteDatabase
import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.attachmentsFromJson
import com.edufelip.shared.domain.model.noteContentFromJson
import com.edufelip.shared.domain.repository.AccountDeletionRepository
import com.edufelip.shared.domain.repository.AccountDeletionResult
import com.edufelip.shared.platform.deleteLocalAttachment
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json

class DefaultAccountDeletionRepository(
    private val authService: AuthService,
    private val database: NoteDatabase,
    private val deletionService: AccountDeletionService = provideAccountDeletionService(),
) : AccountDeletionRepository {
    override suspend fun deleteAccount(): AccountDeletionResult {
        val authenticated = authService.currentUser.firstOrNull()?.uid?.isNotBlank() == true
        if (!authenticated) return AccountDeletionResult.NotAuthenticated
        val idToken = runCatching { authService.getIdToken(forceRefresh = true) }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: return AccountDeletionResult.NotAuthenticated

        val remoteResult = deletionService.deleteAccount(idToken)
        if (remoteResult.isFailure) {
            return AccountDeletionResult.Failure(
                remoteResult.exceptionOrNull()?.message ?: "Failed to delete account.",
            )
        }

        deleteLocalFiles()
        clearLocalDatabase()
        runCatching { authService.signOut() }
        return AccountDeletionResult.Success
    }

    private fun deleteLocalFiles() {
        collectLocalFileUris().forEach { deleteLocalAttachment(it) }
    }

    private fun collectLocalFileUris(): Set<String> {
        val rows = database.noteQueries.selectAll().executeAsList() +
            database.noteQueries.selectDeleted().executeAsList()
        val paths = LinkedHashSet<String>()
        rows.forEach { row ->
            val contentJson = row.content_json?.let(::decryptField)
            val content = noteContentFromJson(contentJson)
            content.blocks
                .filterIsInstance<ImageBlock>()
                .forEach { block ->
                    addIfFileUri(paths, block.localUri)
                    addIfFileUri(paths, block.thumbnailLocalUri)
                    addIfFileUri(paths, block.cachedRemoteUri)
                    addIfFileUri(paths, block.cachedThumbnailUri)
                    addIfFileUri(paths, block.legacyRemoteUri)
                }

            val attachments = attachmentsFromJson(decryptField(row.attachments))
            attachments.forEach { attachment ->
                addIfFileUri(paths, attachment.localUri)
                addIfFileUri(paths, attachment.downloadUrl)
                addIfFileUri(paths, attachment.thumbnailUrl)
            }
        }

        database.noteQueries.selectPendingNoteDeletions().executeAsList().forEach { entry ->
            decodeStoragePaths(entry.storage_paths).forEach { path ->
                addIfFileUri(paths, path)
            }
        }

        database.noteQueries.selectPendingAttachmentDeletions().executeAsList().forEach { path ->
            addIfFileUri(paths, path)
        }
        return paths
    }

    private fun clearLocalDatabase() {
        database.noteQueries.deleteAll()
        database.noteQueries.deleteAllFolders()
        database.noteQueries.deleteAllPendingNoteDeletions()
        database.noteQueries.deleteAllPendingFolderDeletions()
        database.noteQueries.deleteAllPendingAttachmentDeletions()
    }

    private fun addIfFileUri(target: MutableSet<String>, raw: String?) {
        val trimmed = raw?.trim().orEmpty()
        if (trimmed.startsWith("file:", ignoreCase = true)) {
            target.add(trimmed)
        }
    }

    private fun decodeStoragePaths(raw: String?): List<String> = raw
        ?.takeIf { it.isNotBlank() }
        ?.let { runCatching { storagePathsJson.decodeFromString<List<String>>(it) }.getOrDefault(emptyList()) }
        ?: emptyList()
}

private val storagePathsJson = Json { ignoreUnknownKeys = true }
