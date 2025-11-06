package com.edufelip.shared

import androidx.compose.foundation.LocalIndication
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.edufelip.shared.data.auth.GitLiveAuthService
import com.edufelip.shared.data.db.DatabaseDriverFactory
import com.edufelip.shared.data.db.createDatabase
import com.edufelip.shared.data.repository.SqlDelightNoteRepository
import com.edufelip.shared.domain.usecase.buildNoteUseCases
import com.edufelip.shared.domain.validation.NoteValidationRules
import com.edufelip.shared.ui.AmazingNoteApp
import com.edufelip.shared.ui.app.navigation.currentRouteAsState
import com.edufelip.shared.ui.indication.NoFeedbackIndication
import com.edufelip.shared.ui.nav.AppRoutes
import com.edufelip.shared.ui.settings.AppPreferences
import com.edufelip.shared.ui.settings.DefaultAppPreferences
import com.edufelip.shared.ui.settings.Settings
import com.edufelip.shared.ui.vm.DefaultNoteUiViewModel
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIColor
import platform.UIKit.UIViewController
import platform.UIKit.systemBackgroundColor

private object IosSettings : Settings {
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

private val sharedAppPreferences: AppPreferences by lazy {
    DefaultAppPreferences(IosSettings)
}

fun MainViewController(
    tabBarVisibility: ((Boolean) -> Unit)? = null,
    onRouteChanged: ((String) -> Unit)? = null,
): UIViewController = createAmazingNoteViewController(
    initialRoute = AppRoutes.Notes,
    showBottomBar = false,
    tabBarVisibility = tabBarVisibility,
    onRouteChanged = onRouteChanged,
)

fun makeNotesViewController(
    tabBarVisibility: ((Boolean) -> Unit)? = null,
    onRouteChanged: ((String) -> Unit)? = null,
): UIViewController = createAmazingNoteViewController(
    initialRoute = AppRoutes.Notes,
    showBottomBar = false,
    tabBarVisibility = tabBarVisibility,
    onRouteChanged = onRouteChanged,
)

fun makeFoldersViewController(
    tabBarVisibility: ((Boolean) -> Unit)? = null,
    onRouteChanged: ((String) -> Unit)? = null,
): UIViewController = createAmazingNoteViewController(
    initialRoute = AppRoutes.Folders,
    showBottomBar = false,
    tabBarVisibility = tabBarVisibility,
    onRouteChanged = onRouteChanged,
)

fun makeSettingsViewController(
    tabBarVisibility: ((Boolean) -> Unit)? = null,
    onRouteChanged: ((String) -> Unit)? = null,
): UIViewController = createAmazingNoteViewController(
    initialRoute = AppRoutes.Settings,
    showBottomBar = false,
    tabBarVisibility = tabBarVisibility,
    onRouteChanged = onRouteChanged,
)

fun createAmazingNoteViewController(
    initialRoute: AppRoutes,
    showBottomBar: Boolean,
    tabBarVisibility: ((Boolean) -> Unit)? = null,
    onRouteChanged: ((String) -> Unit)? = null,
): UIViewController {
    val controller = ComposeUIViewController {
        val db = createDatabase(DatabaseDriverFactory())
        val repo = SqlDelightNoteRepository(db)
        val useCases = buildNoteUseCases(repo, NoteValidationRules())
        val vm = DefaultNoteUiViewModel(useCases)
        val authService = remember { GitLiveAuthService() }
        val settings = IosSettings
        val appPreferences = remember { sharedAppPreferences }
        CompositionLocalProvider(LocalIndication provides NoFeedbackIndication) {
            val currentRoute by currentRouteAsState()
            LaunchedEffect(currentRoute) {
                onRouteChanged?.invoke(currentRoute)
            }
            AmazingNoteApp(
                viewModel = vm,
                authService = authService,
                settings = settings,
                appPreferences = appPreferences,
                noteDatabase = db,
                initialRoute = initialRoute,
                showBottomBar = showBottomBar,
                onTabBarVisibilityChanged = { isVisible ->
                    tabBarVisibility?.invoke(isVisible)
                },
            )
        }
    }
    return controller.apply {
        view.insetsLayoutMarginsFromSafeArea = false
        view.backgroundColor = UIColor.systemBackgroundColor()
    }
}
