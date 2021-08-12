package com.example.amazing_note.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.amazing_note.models.Priority

@Entity(tableName = "note_table")
data class Note (
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    var title: String,
    var priority: Priority,
    var description: String
)