package com.edufelip.shared.ui.settings

import androidx.compose.runtime.staticCompositionLocalOf
import com.edufelip.shared.model.Priority

interface AppPreferences {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(enabled: Boolean)

    fun isDateModeUpdated(): Boolean
    fun setDateModeUpdated(updated: Boolean)

    fun getPriorityFilter(): Priority?
    fun setPriorityFilter(value: Priority?)
}

class DefaultAppPreferences(private val settings: Settings) : AppPreferences {
    override fun isDarkTheme(): Boolean = settings.getBool(KEY_DARK_THEME, false)
    override fun setDarkTheme(enabled: Boolean) = settings.setBool(KEY_DARK_THEME, enabled)

    override fun isDateModeUpdated(): Boolean = settings.getBool(KEY_DATE_MODE_UPDATED, true)
    override fun setDateModeUpdated(updated: Boolean) = settings.setBool(KEY_DATE_MODE_UPDATED, updated)

    override fun getPriorityFilter(): Priority? = Priority.fromString(settings.getString(KEY_PRIORITY_FILTER, "all"))

    override fun setPriorityFilter(value: Priority?) {
        val v = value?.toString() ?: "all"
        settings.setString(KEY_PRIORITY_FILTER, v)
    }

    private companion object {
        const val KEY_DARK_THEME = "dark_theme"
        const val KEY_DATE_MODE_UPDATED = "date_mode_updated"
        const val KEY_PRIORITY_FILTER = "priority_filter"
    }
}

val LocalAppPreferences = staticCompositionLocalOf<AppPreferences> { DefaultAppPreferences(InMemorySettings()) }
