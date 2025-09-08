package com.edufelip.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.edufelip.shared.ui.AmazingNoteApp
import com.edufelip.shared.data.SqlDelightNoteRepository
import com.edufelip.shared.db.DatabaseDriverFactory
import com.edufelip.shared.db.createDatabase
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    val db = createDatabase(DatabaseDriverFactory())
    val repo = SqlDelightNoteRepository(db)
    AmazingNoteApp(noteRepository = repo)
}
