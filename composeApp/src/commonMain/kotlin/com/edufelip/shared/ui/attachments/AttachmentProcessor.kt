package com.edufelip.shared.ui.attachments

import androidx.compose.runtime.Composable

/**
 * Request describing a local image that should be processed into multiple renditions
 * before syncing to Firebase Storage.
 */
data class AttachmentProcessingRequest(
    val sourceUri: String,
    val mimeType: String?,
    val width: Int?,
    val height: Int?,
)

enum class RenditionType { Tiny, Display, Original }

data class AttachmentRendition(
    val type: RenditionType,
    val localUri: String,
    val mimeType: String,
    val width: Int?,
    val height: Int?,
    val sizeBytes: Long,
    val sha256: String?,
)

data class AttachmentProcessingResult(
    val original: AttachmentRendition,
    val display: AttachmentRendition?,
    val tiny: AttachmentRendition?,
)

interface AttachmentProcessor {
    suspend fun process(request: AttachmentProcessingRequest): AttachmentProcessingResult
}

@Composable
expect fun rememberAttachmentProcessor(): AttachmentProcessor?
