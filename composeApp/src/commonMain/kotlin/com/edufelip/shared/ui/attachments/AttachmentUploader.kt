package com.edufelip.shared.ui.attachments

import com.edufelip.shared.domain.model.NoteAttachment
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.storage.storage
import kotlin.random.Random
import dev.gitlive.firebase.storage.File as StorageFile

data class AttachmentUploadPayload(
    val file: StorageFile,
    val mimeType: String,
    val fileName: String?,
    val width: Int?,
    val height: Int?,
    val cleanUp: (() -> Unit)? = null,
)

private const val ATTACHMENTS_FOLDER = "attachments"

suspend fun uploadAttachmentWithGitLive(
    payload: AttachmentUploadPayload,
    onProgress: (Float, String?) -> Unit,
): NoteAttachment {
    val storage = Firebase.storage
    val id = generateAttachmentId()
    val path = "$ATTACHMENTS_FOLDER/$id"
    val reference = storage.reference.child(path)

    try {
        onProgress(0f, payload.fileName)

        reference.putFile(payload.file)

        val downloadUrl = reference.getDownloadUrl()
        onProgress(1f, payload.fileName)

        return NoteAttachment(
            id = id,
            downloadUrl = downloadUrl,
            thumbnailUrl = null,
            mimeType = payload.mimeType,
            fileName = payload.fileName ?: "$id.${inferFileExtension(payload.mimeType)}",
            width = payload.width,
            height = payload.height,
        )
    } finally {
        payload.cleanUp?.invoke()
    }
}

private fun generateAttachmentId(): String {
    val alphabet = "abcdefghijklmnopqrstuvwxyz0123456789"
    return buildString(20) {
        repeat(20) {
            append(alphabet[Random.nextInt(alphabet.length)])
        }
    }
}

private fun inferFileExtension(mimeType: String): String = when {
    mimeType.contains("png", ignoreCase = true) -> "png"
    mimeType.contains("jpeg", ignoreCase = true) || mimeType.contains("jpg", ignoreCase = true) -> "jpg"
    mimeType.contains("heic", ignoreCase = true) -> "heic"
    else -> "img"
}

expect fun storageFileForLocalUri(localUri: String): StorageFile

expect fun deleteLocalAttachment(localUri: String)
