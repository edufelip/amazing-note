package com.edufelip.shared.ui.settings

import androidx.compose.runtime.staticCompositionLocalOf
import com.edufelip.shared.ui.components.organisms.notes.FolderLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AppPreferences {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(enabled: Boolean)

    val darkThemeFlow: StateFlow<Boolean>

    fun isDateModeUpdated(): Boolean
    fun setDateModeUpdated(updated: Boolean)

    fun folderLayout(): FolderLayout
    fun setFolderLayout(layout: FolderLayout)
    val folderLayoutFlow: StateFlow<FolderLayout>
}

class DefaultAppPreferences(private val settings: Settings) : AppPreferences {
    private val darkThemeState = MutableStateFlow(settings.getBool(KEY_DARK_THEME, true))
    private val folderLayoutState = MutableStateFlow(loadFolderLayout())

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

    private fun loadFolderLayout(): FolderLayout {
        val stored = settings.getString(KEY_FOLDERS_LAYOUT, FolderLayout.Grid.name)
        return runCatching { FolderLayout.valueOf(stored) }.getOrDefault(FolderLayout.Grid)
    }

    override fun folderLayout(): FolderLayout = folderLayoutState.value

    override fun setFolderLayout(layout: FolderLayout) {
        if (folderLayoutState.value != layout) {
            folderLayoutState.value = layout
        }
        settings.setString(KEY_FOLDERS_LAYOUT, layout.name)
    }

    override val folderLayoutFlow: StateFlow<FolderLayout> = folderLayoutState.asStateFlow()

    private companion object {
        const val KEY_DARK_THEME = "dark_theme"
        const val KEY_DATE_MODE_UPDATED = "date_mode_updated"
        const val KEY_FOLDERS_LAYOUT = "folders_layout"
    }
}

val LocalAppPreferences = staticCompositionLocalOf<AppPreferences> { DefaultAppPreferences(InMemorySettings()) }
