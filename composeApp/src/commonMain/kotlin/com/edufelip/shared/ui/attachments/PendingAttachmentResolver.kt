package com.edufelip.shared.ui.attachments

import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.NoteContent

suspend fun NoteContent.resolvePendingImageAttachments(
    uploader: suspend (ImageBlock) -> UploadedImage = { block ->
        val payload = AttachmentUploadPayload(
            file = storageFileForLocalUri(block.uri),
            mimeType = block.mimeType ?: "image/*",
            fileName = block.fileName ?: block.alt ?: "image_${block.id}",
            width = block.width,
            height = block.height,
            cleanUp = null,
        )
        val uploaded = uploadAttachmentWithGitLive(payload) { _, _ -> }
        UploadedImage(uploaded.downloadUrl, uploaded.thumbnailUrl)
    },
    onCleanup: (String) -> Unit = { deleteLocalAttachment(it) },
): NoteContent {
    if (blocks.isEmpty()) return this
    var mutated = false
    val updatedBlocks = blocks.map { block ->
        if (block is ImageBlock && block.remoteUri.isNullOrBlank()) {
            val uploaded = uploader(block)
            onCleanup(block.uri)
            mutated = true
            block.copy(
                uri = uploaded.remoteUrl,
                remoteUri = uploaded.remoteUrl,
                thumbnailUri = uploaded.thumbnailUrl,
            )
        } else {
            block
        }
    }
    return if (mutated) copy(blocks = updatedBlocks) else this
}

data class UploadedImage(
    val remoteUrl: String,
    val thumbnailUrl: String?,
)
