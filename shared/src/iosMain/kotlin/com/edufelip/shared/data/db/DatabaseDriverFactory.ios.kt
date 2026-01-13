package com.edufelip.shared.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.edufelip.shared.db.NoteDatabase

private val DATABASE_NAME = "notes.v${NoteDatabase.Schema.version}.db"

actual class DatabaseDriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver = runCatching { NativeSqliteDriver(NoteDatabase.Schema, DATABASE_NAME) }
        .getOrElse { error ->
            val isVersionMismatch = error.message
                ?.contains("Database version", ignoreCase = true) == true
            if (!isVersionMismatch) {
                throw error
            }
            NativeSqliteDriver(NoteDatabase.Schema, DATABASE_NAME)
        }
}
