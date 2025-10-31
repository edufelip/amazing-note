package com.edufelip.shared

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.edufelip.shared.data.auth.GitLiveAuthService
import com.edufelip.shared.data.db.DatabaseDriverFactory
import com.edufelip.shared.data.db.createDatabase
import com.edufelip.shared.data.repository.SqlDelightNoteRepository
import com.edufelip.shared.domain.usecase.buildNoteUseCases
import com.edufelip.shared.domain.validation.NoteValidationRules
import com.edufelip.shared.ui.AmazingNoteApp
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.settings.DefaultAppPreferences
import com.edufelip.shared.ui.settings.Settings
import com.edufelip.shared.ui.vm.DefaultNoteUiViewModel
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIColor
import platform.UIKit.UIViewController
import platform.UIKit.systemBackgroundColor

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

fun MainViewController(): UIViewController = createAmazingNoteViewController(
    initialRoute = AppRoutes.Notes,
    showBottomBar = false,
)

fun makeNotesViewController(): UIViewController = createAmazingNoteViewController(
    initialRoute = AppRoutes.Notes,
    showBottomBar = false,
)

fun makeFoldersViewController(): UIViewController = createAmazingNoteViewController(
    initialRoute = AppRoutes.Folders,
    showBottomBar = false,
)

fun makeSettingsViewController(): UIViewController = createAmazingNoteViewController(
    initialRoute = AppRoutes.Settings,
    showBottomBar = false,
)

fun createAmazingNoteViewController(
    initialRoute: AppRoutes,
    showBottomBar: Boolean,
): UIViewController {
    val controller = ComposeUIViewController {
        val db = createDatabase(DatabaseDriverFactory())
        val repo = SqlDelightNoteRepository(db)
        val useCases = buildNoteUseCases(repo, NoteValidationRules())
        val vm = DefaultNoteUiViewModel(useCases)
        val authService = remember { GitLiveAuthService() }
        val settings = remember { IosSettings() }
        val appPreferences = remember(settings) { DefaultAppPreferences(settings) }
        AmazingNoteApp(
            viewModel = vm,
            authService = authService,
            settings = settings,
            appPreferences = appPreferences,
            noteDatabase = db,
            initialRoute = initialRoute,
            showBottomBar = showBottomBar,
        )
    }
    return controller.apply {
        view.insetsLayoutMarginsFromSafeArea = false
        view.backgroundColor = UIColor.systemBackgroundColor()
    }
}
