package com.edufelip.shared.ui.settings

import androidx.compose.runtime.staticCompositionLocalOf

interface AppPreferences {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(enabled: Boolean)

    fun isDateModeUpdated(): Boolean
    fun setDateModeUpdated(updated: Boolean)
}

class DefaultAppPreferences(private val settings: Settings) : AppPreferences {
    override fun isDarkTheme(): Boolean = settings.getBool(KEY_DARK_THEME, true)
    override fun setDarkTheme(enabled: Boolean) = settings.setBool(KEY_DARK_THEME, enabled)

    override fun isDateModeUpdated(): Boolean = settings.getBool(KEY_DATE_MODE_UPDATED, true)
    override fun setDateModeUpdated(updated: Boolean) = settings.setBool(KEY_DATE_MODE_UPDATED, updated)

    private companion object {
        const val KEY_DARK_THEME = "dark_theme"
        const val KEY_DATE_MODE_UPDATED = "date_mode_updated"
    }
}

val LocalAppPreferences = staticCompositionLocalOf<AppPreferences> { DefaultAppPreferences(InMemorySettings()) }
