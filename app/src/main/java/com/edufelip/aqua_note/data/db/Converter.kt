package com.edufelip.aqua_note.data.db

import androidx.room.TypeConverter
import com.edufelip.aqua_note.data.models.Priority

class Converter {
    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(priority: String): Priority {
        return Priority.valueOf(priority)
    }
}