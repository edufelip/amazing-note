package com.edufelip.shared.ui.attachments

import com.edufelip.shared.domain.model.ImageBlock
import com.edufelip.shared.domain.model.NoteAttachment
import com.edufelip.shared.platform.storageFileForLocalUri

data class UploadContext(
    val userId: String,
    val noteStableId: String,
)

class AttachmentUploadCoordinator(
    private val uploader: suspend (AttachmentUploadPayload) -> NoteAttachment = { payload ->
        uploadAttachmentWithGitLive(payload) { _, _ -> }
    },
) {
    suspend fun upload(
        context: UploadContext,
        block: ImageBlock,
        renditions: AttachmentProcessingResult?,
    ): UploadedImage {
        val displayRendition = renditions?.display ?: renditions?.original
        val target = displayRendition ?: renditionFromBlock(block)
        val displayUpload = uploadRendition(
            context = context,
            blockId = block.id,
            rendition = target,
            suffix = null,
        )
        val thumbUpload = renditions?.tiny?.let { tiny ->
            uploadRendition(
                context = context,
                blockId = block.id,
                rendition = tiny,
                suffix = "thumb",
            )
        }
        return UploadedImage(
            remoteUrl = displayUpload.attachment.downloadUrl,
            thumbnailUrl = thumbUpload?.attachment?.downloadUrl,
            storagePath = displayUpload.storagePath,
            thumbnailStoragePath = thumbUpload?.storagePath,
        )
    }

    private data class StoredUpload(
        val attachment: NoteAttachment,
        val storagePath: String,
    )

    private suspend fun uploadRendition(
        context: UploadContext,
        blockId: String,
        rendition: AttachmentRendition,
        suffix: String?,
    ): StoredUpload {
        val fileName = buildFileName(blockId, suffix, rendition.mimeType)
        val storagePath = buildStoragePath(context, fileName)
        val payload = AttachmentUploadPayload(
            file = storageFileForLocalUri(rendition.localUri),
            mimeType = rendition.mimeType,
            fileName = fileName,
            width = rendition.width,
            height = rendition.height,
            storagePath = storagePath,
            cleanUp = null,
        )
        val uploaded = uploader(payload)
        return StoredUpload(uploaded, storagePath)
    }

    private fun buildFileName(blockId: String, suffix: String?, mimeType: String): String {
        val ext = inferFileExtension(mimeType)
        return if (suffix.isNullOrBlank()) {
            "$blockId.$ext"
        } else {
            "${blockId}_$suffix.$ext"
        }
    }

    private fun buildStoragePath(context: UploadContext, fileName: String): String =
        "images/${context.userId}/${context.noteStableId}/$fileName"

    private fun renditionFromBlock(block: ImageBlock): AttachmentRendition {
        val sourceUri = block.localUri ?: block.uri
        return AttachmentRendition(
            type = RenditionType.Display,
            localUri = sourceUri,
            mimeType = block.mimeType ?: "image/*",
            width = block.width,
            height = block.height,
            sizeBytes = 0,
            sha256 = null,
        )
    }
}
