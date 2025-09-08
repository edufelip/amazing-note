package com.edufelip.shared.db

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlDriver

object AndroidContextHolder {
    lateinit var appContext: Context
}

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(NoteDatabase.Schema, AndroidContextHolder.appContext, "notes.db")
    }
}
