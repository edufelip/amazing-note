package com.edufelip.shared

import androidx.compose.ui.window.ComposeUIViewController
import com.edufelip.shared.auth.IosFirebaseAuthService
import com.edufelip.shared.auth.IosGoogleSignIn
import com.edufelip.shared.data.SqlDelightNoteRepository
import com.edufelip.shared.db.DatabaseDriverFactory
import com.edufelip.shared.db.createDatabase
import com.edufelip.shared.domain.usecase.buildNoteUseCases
import com.edufelip.shared.domain.validation.NoteValidationRules
import com.edufelip.shared.presentation.DefaultNoteUiViewModel
import com.edufelip.shared.ui.AmazingNoteApp
import com.edufelip.shared.ui.settings.DefaultAppPreferences
import com.edufelip.shared.ui.settings.Settings
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIViewController

private class IosSettings : Settings {
    private val defaults = NSUserDefaults.standardUserDefaults()
    override fun getBool(key: String, default: Boolean): Boolean {
        val obj = defaults.objectForKey(key)
        return if (obj == null) default else defaults.boolForKey(key)
    }
    override fun setBool(key: String, value: Boolean) {
        defaults.setBool(value, forKey = key)
    }
    override fun getString(key: String, default: String): String {
        val obj = defaults.stringForKey(key)
        return obj ?: default
    }
    override fun setString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }
}

fun MainViewController(): UIViewController = ComposeUIViewController {
    val db = createDatabase(DatabaseDriverFactory())
    val repo = SqlDelightNoteRepository(db)
    val useCases = buildNoteUseCases(repo, NoteValidationRules())
    val vm = DefaultNoteUiViewModel(useCases)
    AmazingNoteApp(
        viewModel = vm,
        authService = IosFirebaseAuthService(),
        onRequestGoogleSignIn = { cb -> IosGoogleSignIn.requestSignIn { success, err -> cb(success, err) } },
        settings = IosSettings(),
        appPreferences = DefaultAppPreferences(IosSettings()),
    )
}
