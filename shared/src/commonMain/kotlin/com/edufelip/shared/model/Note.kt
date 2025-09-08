package com.edufelip.shared.model

data class Note(
    val id: Int,
    val title: String,
    val priority: Priority,
    val description: String,
    val deleted: Boolean
)

