package com.edufelip.shared.ui.attachments

import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.ImageSyncState
import com.edufelip.shared.domain.model.NoteContent

suspend fun NoteContent.resolvePendingImageAttachments(
    uploader: suspend (ImageBlock) -> UploadedImage,
    onCleanup: (String) -> Unit = {},
): NoteContent {
    if (blocks.isEmpty()) return this
    var mutated = false
    val updatedBlocks = blocks.map { block ->
        if (block is ImageBlock && block.requiresUpload()) {
            val uploadingBlock = block.copy(syncState = ImageSyncState.Uploading)
            val uploadResult = runCatching { uploader(uploadingBlock) }
            mutated = true
            uploadResult.fold(
                onSuccess = { uploaded ->
                    uploadingBlock.localUri?.let(onCleanup)
                    uploadingBlock.copy(
                        storagePath = uploaded.storagePath,
                        thumbnailStoragePath = uploaded.thumbnailStoragePath ?: block.thumbnailStoragePath,
                        legacyUri = uploaded.storagePath,
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

private fun ImageBlock.requiresUpload(): Boolean {
    return storagePath.isNullOrBlank() || syncState != ImageSyncState.Synced
}

data class UploadedImage(
    val remoteUrl: String,
    val thumbnailUrl: String?,
    val storagePath: String,
    val thumbnailStoragePath: String? = null,
)
