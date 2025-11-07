package com.edufelip.shared.ui.attachments

import com.edufelip.shared.domain.model.ImageBlock
class AttachmentUploadCoordinator(
    private val uploader: suspend (AttachmentUploadPayload) -> com.edufelip.shared.domain.model.NoteAttachment = { payload ->
        uploadAttachmentWithGitLive(payload) { _, _ -> }
    },
) {
    suspend fun upload(block: ImageBlock, result: AttachmentProcessingResult): UploadedImage {
        // tiny for instant previews, ignore remote URL
        result.tiny?.let { uploadRendition(block, it) }

        val displayCandidate = result.display ?: result.original
        val displayUpload = displayCandidate?.let { uploadRendition(block, it) }
            ?: uploadFallback(block)

        if (result.original != null && result.original != displayCandidate) {
            uploadRendition(block, result.original)
        }

        return UploadedImage(
            remoteUrl = displayUpload.downloadUrl,
            thumbnailUrl = displayUpload.thumbnailUrl,
        )
    }

    private suspend fun uploadRendition(block: ImageBlock, rendition: AttachmentRendition): com.edufelip.shared.domain.model.NoteAttachment {
        val payload = AttachmentUploadPayload(
            file = storageFileForLocalUri(rendition.localUri),
            mimeType = rendition.mimeType,
            fileName = block.fileName ?: block.alt ?: "image_${block.id}",
            width = rendition.width,
            height = rendition.height,
            cleanUp = null,
        )
        return uploader(payload)
    }

    private suspend fun uploadFallback(block: ImageBlock): com.edufelip.shared.domain.model.NoteAttachment {
        val payload = AttachmentUploadPayload(
            file = storageFileForLocalUri(block.uri),
            mimeType = block.mimeType ?: "image/*",
            fileName = block.fileName ?: block.alt ?: "image_${block.id}",
            width = block.width,
            height = block.height,
            cleanUp = null,
        )
        return uploader(payload)
    }
}
