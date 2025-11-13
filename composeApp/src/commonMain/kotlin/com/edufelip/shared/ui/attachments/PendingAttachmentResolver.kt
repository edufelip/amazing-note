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
            val uploadResult = runCatching { uploader(block) }
            mutated = true
            uploadResult.fold(
                onSuccess = { uploaded ->
                    block.copy(
                        uri = uploaded.remoteUrl,
                        remoteUri = uploaded.remoteUrl,
                        thumbnailUri = uploaded.thumbnailUrl ?: block.thumbnailUri,
                        storagePath = uploaded.storagePath,
                        syncState = ImageSyncState.Synced,
                    )
                },
                onFailure = {
                    block.copy(syncState = ImageSyncState.Error)
                },
            )
        } else {
            block
        }
    }
    return if (mutated) copy(blocks = updatedBlocks) else this
}

private fun ImageBlock.requiresUpload(): Boolean {
    val remoteMissing = remoteUri.isNullOrBlank() && (uri.isBlank() || uri.startsWith("file:"))
    return syncState != ImageSyncState.Synced || remoteMissing || storagePath.isNullOrBlank()
}

data class UploadedImage(
    val remoteUrl: String,
    val thumbnailUrl: String?,
    val storagePath: String,
    val thumbnailStoragePath: String? = null,
)
