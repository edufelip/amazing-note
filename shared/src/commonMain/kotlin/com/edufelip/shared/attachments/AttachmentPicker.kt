package com.edufelip.shared.attachments

import androidx.compose.runtime.Composable
import com.edufelip.shared.model.NoteAttachment

fun interface AttachmentPicker {
    suspend fun pickImage(onProgress: (progress: Float, fileName: String?) -> Unit): NoteAttachment?
}

suspend fun AttachmentPicker.pickImage(): NoteAttachment? = pickImage { _, _ -> }

@Composable
expect fun rememberAttachmentPicker(): AttachmentPicker?
