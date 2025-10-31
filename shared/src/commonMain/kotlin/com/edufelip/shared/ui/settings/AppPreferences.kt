package com.edufelip.shared.ui.settings

import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AppPreferences {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(enabled: Boolean)

    val darkThemeFlow: StateFlow<Boolean>

    fun isDateModeUpdated(): Boolean
    fun setDateModeUpdated(updated: Boolean)
}

class DefaultAppPreferences(private val settings: Settings) : AppPreferences {
    private val darkThemeState = MutableStateFlow(settings.getBool(KEY_DARK_THEME, true))

    override val darkThemeFlow: StateFlow<Boolean> = darkThemeState.asStateFlow()

    override fun isDarkTheme(): Boolean = darkThemeState.value

    override fun setDarkTheme(enabled: Boolean) {
        if (darkThemeState.value != enabled) {
            darkThemeState.value = enabled
        }
        settings.setBool(KEY_DARK_THEME, enabled)
    }

    override fun isDateModeUpdated(): Boolean = settings.getBool(KEY_DATE_MODE_UPDATED, true)
    override fun setDateModeUpdated(updated: Boolean) = settings.setBool(KEY_DATE_MODE_UPDATED, updated)

    private companion object {
        const val KEY_DARK_THEME = "dark_theme"
        const val KEY_DATE_MODE_UPDATED = "date_mode_updated"
    }
}

val LocalAppPreferences = staticCompositionLocalOf<AppPreferences> { DefaultAppPreferences(InMemorySettings()) }
