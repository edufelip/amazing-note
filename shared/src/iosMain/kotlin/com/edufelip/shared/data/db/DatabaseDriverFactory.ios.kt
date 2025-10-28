package com.edufelip.shared.data.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.edufelip.shared.db.NoteDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

private val DATABASE_NAME = "notes.v${NoteDatabase.Companion.Schema.version}.db"

private val LEGACY_DATABASE_FILES = listOf(
    "notes.db",
    "notes.v0.db",
    "notes.v1.db",
    DATABASE_NAME,
)

actual class DatabaseDriverFactory actual constructor() {
    @OptIn(ExperimentalForeignApi::class)
    actual fun createDriver(): SqlDriver = runCatching { NativeSqliteDriver(NoteDatabase.Companion.Schema, DATABASE_NAME) }
        .getOrElse { error ->
            if (!error.message.orEmpty().contains("Database version", ignoreCase = true)) {
                throw error
            }
            println("notes.db schema mismatch detected; clearing local cache and recreating database.")
            resetDatabase()
            NativeSqliteDriver(NoteDatabase.Companion.Schema, DATABASE_NAME)
        }
}

@OptIn(ExperimentalForeignApi::class)
private fun resetDatabase() {
    val manager = NSFileManager.defaultManager

    LEGACY_DATABASE_FILES
        .map { cachePath(it) }
        .forEach { manager.removeItemAtPath(it, null) }

    val legacyDocumentsDir = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true,
    ).firstOrNull() as? String

    if (legacyDocumentsDir != null) {
        LEGACY_DATABASE_FILES
            .map { "$legacyDocumentsDir/$it" }
            .forEach { manager.removeItemAtPath(it, null) }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun cachePath(fileName: String): String {
    val cacheDir = NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory,
        NSUserDomainMask,
        true,
    ).firstOrNull() as? String

    val base = cacheDir ?: "${NSHomeDirectory()}/Library/Caches"
    return "$base/$fileName"
}
