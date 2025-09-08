package com.edufelip.amazing_note.data.mappers

import com.edufelip.amazing_note.data.models.Note as AndroidNote
import com.edufelip.amazing_note.data.models.Priority as AndroidPriority
import com.edufelip.shared.model.Note as SharedNote
import com.edufelip.shared.model.Priority as SharedPriority

fun SharedNote.toAndroid(): AndroidNote = AndroidNote(
    id = id,
    title = title,
    priority = priority.toAndroid(),
    description = description,
    deleted = deleted
)

fun AndroidNote.toShared(): SharedNote = SharedNote(
    id = id,
    title = title,
    priority = priority.toShared(),
    description = description,
    deleted = deleted
)

fun SharedPriority.toAndroid(): AndroidPriority = when (this) {
    SharedPriority.HIGH -> AndroidPriority.HIGH
    SharedPriority.MEDIUM -> AndroidPriority.MEDIUM
    SharedPriority.LOW -> AndroidPriority.LOW
}

fun AndroidPriority.toShared(): SharedPriority = when (this) {
    AndroidPriority.HIGH -> SharedPriority.HIGH
    AndroidPriority.MEDIUM -> SharedPriority.MEDIUM
    AndroidPriority.LOW -> SharedPriority.LOW
}

