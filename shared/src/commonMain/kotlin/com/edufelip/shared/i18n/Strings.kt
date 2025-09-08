package com.edufelip.shared.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

enum class Str {
    YourNotes,
    Trash,
    PrivacyPolicy,
    Title,
    Description,
    Search,
    HighPriority,
    MediumPriority,
    LowPriority,
    // Content descriptions (accessibility)
    CdOpenDrawer,
    CdAdd,
    CdBack,
    CdSave,
    CdDelete,
    CdRestore,
    CdSearch,
    CdClearSearch,
    CdToggleDarkTheme,
    ErrorTitleRequired,
    ErrorDescriptionRequired,
    ErrorTitleTooLong,       // arg: max
    ErrorDescriptionTooLong  // arg: max
}

interface Strings {
    fun get(id: Str, vararg args: Any): String
}

val LocalStrings = staticCompositionLocalOf<Strings> {
    object : Strings {
        override fun get(id: Str, vararg args: Any): String = when (id) {
            Str.YourNotes -> "Your Notes"
            Str.Trash -> "Trash"
            Str.PrivacyPolicy -> "Privacy Policy"
            Str.Title -> "Title"
            Str.Description -> "Description"
            Str.Search -> "Search"
            Str.HighPriority -> "High Priority"
            Str.MediumPriority -> "Medium Priority"
            Str.LowPriority -> "Low Priority"
            Str.CdOpenDrawer -> "Open navigation drawer"
            Str.CdAdd -> "Add"
            Str.CdBack -> "Back"
            Str.CdSave -> "Save"
            Str.CdDelete -> "Delete"
            Str.CdRestore -> "Restore"
            Str.CdSearch -> "Search"
            Str.CdClearSearch -> "Clear search"
            Str.CdToggleDarkTheme -> "Toggle dark theme"
            Str.ErrorTitleRequired -> "Title is required"
            Str.ErrorDescriptionRequired -> "Description is required"
            Str.ErrorTitleTooLong -> "Title too long (max ${args.firstOrNull() ?: ""})"
            Str.ErrorDescriptionTooLong -> "Description too long (max ${args.firstOrNull() ?: ""})"
        }
    }
}

@Composable
fun string(id: Str, vararg args: Any): String = LocalStrings.current.get(id, *args)
