package com.edufelip.shared.model

data class Note(
    val id: Int,
    val title: String,
    val description: String,
    val deleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val dirty: Boolean = false,
    val localUpdatedAt: Long = 0L,
    val folderId: Long? = null,
    val descriptionSpans: List<NoteTextSpan> = emptyList(),
    val attachments: List<NoteAttachment> = emptyList(),
    val blocks: List<NoteBlock> = emptyList(),
)
