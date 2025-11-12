package com.edufelip.shared.domain.model

data class Folder(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val deleted: Boolean = false,
    val dirty: Boolean = false,
    val localUpdatedAt: Long = 0,
)
