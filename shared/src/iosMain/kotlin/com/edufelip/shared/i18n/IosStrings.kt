package com.edufelip.shared.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import platform.Foundation.NSBundle

private class IosStrings : Strings {
    override fun get(id: Str, vararg args: Any): String {
        val key = when (id) {
            Str.YourNotes -> "your_notes"
            Str.Trash -> "trash"
            Str.PrivacyPolicy -> "privacy_policy"
            Str.Title -> "title"
            Str.Description -> "description"
            Str.Search -> "search"
            Str.HighPriority -> "high_priority"
            Str.MediumPriority -> "medium_priority"
            Str.LowPriority -> "low_priority"
            Str.CdOpenDrawer -> "cd_open_drawer"
            Str.CdAdd -> "cd_add"
            Str.CdBack -> "cd_back"
            Str.CdSave -> "cd_save"
            Str.CdDelete -> "cd_delete"
            Str.CdRestore -> "cd_restore"
            Str.CdSearch -> "cd_search"
            Str.CdClearSearch -> "cd_clear_search"
            Str.CdToggleDarkTheme -> "cd_toggle_dark_theme"
            Str.ErrorTitleRequired -> "error_title_required"
            Str.ErrorDescriptionRequired -> "error_description_required"
            Str.ErrorTitleTooLong -> "error_title_too_long"
            Str.ErrorDescriptionTooLong -> "error_description_too_long"
        }
        val raw = NSBundle.mainBundle.localizedStringForKey(key = key, value = key, table = null)
        return if (args.isNotEmpty()) raw.format(*args) else raw
    }
}

@Composable
fun ProvideIosStrings(content: @Composable () -> Unit) {
    val strings = remember { IosStrings() }
    CompositionLocalProvider(LocalStrings provides strings, content = content)
}

private fun String.format(vararg args: Any?): String {
    var out = this
    args.forEachIndexed { idx, any ->
        out = out.replace("%${idx + 1}$d", any?.toString() ?: "")
        out = out.replace("%d", any?.toString() ?: "")
    }
    return out
}
