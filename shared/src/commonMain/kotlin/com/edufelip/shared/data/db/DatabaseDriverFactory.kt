package com.edufelip.shared.db

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory() {
    fun createDriver(): SqlDriver
}

fun createDatabase(factory: DatabaseDriverFactory): NoteDatabase = NoteDatabase(factory.createDriver())
