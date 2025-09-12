package com.edufelip.shared.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

object AndroidContextHolder {
    lateinit var appContext: Context
}

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver = AndroidSqliteDriver(NoteDatabase.Schema, AndroidContextHolder.appContext, "notes.db")
}
