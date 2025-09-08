package com.edufelip.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.edufelip.shared.data.SqlDelightNoteRepository
import com.edufelip.shared.db.DatabaseDriverFactory
import com.edufelip.shared.db.createDatabase
import com.edufelip.shared.domain.usecase.buildNoteUseCases
import com.edufelip.shared.domain.validation.NoteValidationRules
import com.edufelip.shared.presentation.DefaultNoteUiViewModel
import com.edufelip.shared.i18n.ProvideIosStrings
import com.edufelip.shared.ui.AmazingNoteApp
import platform.UIKit.UIViewController

fun MainViewController(): UIViewController = ComposeUIViewController {
    val db = createDatabase(DatabaseDriverFactory())
    val repo = SqlDelightNoteRepository(db)
    val useCases = buildNoteUseCases(repo, NoteValidationRules())
    val vm = DefaultNoteUiViewModel(useCases)
    ProvideIosStrings {
        AmazingNoteApp(viewModel = vm)
    }
}
