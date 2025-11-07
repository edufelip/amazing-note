package com.edufelip.shared.ui.attachments

import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.NoteContent

suspend fun NoteContent.resolvePendingImageAttachments(): NoteContent {
    if (blocks.isEmpty()) return this
    var mutated = false
    val updatedBlocks = blocks.map { block ->
        if (block is ImageBlock && block.remoteUri.isNullOrBlank()) {
            val payload = AttachmentUploadPayload(
                file = storageFileForLocalUri(block.uri),
                mimeType = block.mimeType ?: "image/*",
                fileName = block.fileName ?: block.alt ?: "image_${block.id}",
                width = block.width,
                height = block.height,
            )
            val uploaded = uploadAttachmentWithGitLive(payload) { _, _ -> }
            deleteLocalAttachment(block.uri)
            mutated = true
            block.copy(
                uri = uploaded.downloadUrl,
                remoteUri = uploaded.downloadUrl,
                thumbnailUri = uploaded.thumbnailUrl,
            )
        } else {
            block
        }
    }
    return if (mutated) copy(blocks = updatedBlocks) else this
}
