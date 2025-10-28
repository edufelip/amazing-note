package com.edufelip.shared.data.db

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.edufelip.shared.db.NoteDatabase

object AndroidContextHolder {
    lateinit var appContext: Context
}

actual class DatabaseDriverFactory actual constructor() {
    actual fun createDriver(): SqlDriver = AndroidSqliteDriver(NoteDatabase.Companion.Schema, AndroidContextHolder.appContext, "notes.db")
}
