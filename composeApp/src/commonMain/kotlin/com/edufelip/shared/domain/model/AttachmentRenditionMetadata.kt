package com.edufelip.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class AttachmentRenditionType {
    Tiny,
    Display,
    Original,
}

@Serializable
data class AttachmentRenditionMetadata(
    val type: AttachmentRenditionType,
    val format: String,
    val width: Int?,
    val height: Int?,
    val bytes: Long,
    val contentType: String,
    val downloadUrl: String,
    val gsPath: String? = null,
    val createdAt: Long? = null,
    val toolVersion: Int? = null,
    val sourceHash: String? = null,
)

@Serializable
data class AttachmentMetadata(
    val attachmentId: String,
    val ownerUid: String,
    val noteId: String,
    val logicalName: String? = null,
    val originalFilename: String? = null,
    val hashes: AttachmentHashes? = null,
    val dimensionsOriginal: AttachmentDimensions? = null,
    val mimeOriginal: String? = null,
    val sizeOriginal: Long? = null,
    val renditions: List<AttachmentRenditionMetadata> = emptyList(),
    val placeholder: AttachmentPlaceholder? = null,
)

@Serializable
data class AttachmentHashes(
    val sha256: String? = null,
    val perceptual: String? = null,
)

@Serializable
data class AttachmentDimensions(
    val width: Int?,
    val height: Int?,
)

@Serializable
data class AttachmentPlaceholder(
    val blurHash: String? = null,
    val averageColor: Long? = null,
)
