package com.edufelip.shared.data.db

import app.cash.sqldelight.db.SqlDriver
import com.edufelip.shared.db.NoteDatabase

expect class DatabaseDriverFactory() {
    fun createDriver(): SqlDriver
}

fun createDatabase(factory: DatabaseDriverFactory): NoteDatabase = NoteDatabase.Companion(factory.createDriver())
