package com.edufelip.shared.ui.attachments

import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.ImageSyncState
import com.edufelip.shared.domain.model.NoteContent
import com.edufelip.shared.ui.attachments.platform.localFileExists

suspend fun NoteContent.resolvePendingImageAttachments(
    uploader: suspend (ImageBlock) -> UploadedImage,
    onCleanup: (String) -> Unit = {},
): NoteContent {
    if (blocks.isEmpty()) return this
    var mutated = false
    val updatedBlocks = blocks.map { block ->
        if (block is ImageBlock && block.requiresUpload()) {
            val rawSourceUri = block.localUri
            if (rawSourceUri.isNullOrBlank()) {
                mutated = true
                return@map block.copy(syncState = ImageSyncState.UploadFailed)
            }
            if (rawSourceUri.startsWith("file:", ignoreCase = true) && !localFileExists(rawSourceUri)) {
                mutated = true
                return@map block.copy(syncState = ImageSyncState.UploadFailed)
            }
            val uploadingBlock = block.copy(
                syncState = ImageSyncState.Uploading,
                localUri = rawSourceUri,
            )
            val uploadResult = runCatching { uploader(uploadingBlock) }
            mutated = true
            uploadResult.fold(
                onSuccess = { uploaded ->
                    uploadingBlock.localUri?.let(onCleanup)
                    uploadingBlock.copy(
                        storagePath = uploaded.storagePath,
                        thumbnailStoragePath = uploaded.thumbnailStoragePath ?: block.thumbnailStoragePath,
                        thumbnailLocalUri = block.thumbnailLocalUri ?: block.thumbnailUri,
                        syncState = ImageSyncState.Synced,
                    )
                },
                onFailure = {
                    uploadingBlock.copy(syncState = ImageSyncState.UploadFailed)
                },
            )
        } else {
            block
        }
    }
    return if (mutated) copy(blocks = updatedBlocks) else this
}

private fun ImageBlock.requiresUpload(): Boolean = storagePath.isNullOrBlank() || syncState != ImageSyncState.Synced

data class UploadedImage(
    val remoteUrl: String,
    val thumbnailUrl: String?,
    val storagePath: String,
    val thumbnailStoragePath: String? = null,
)
