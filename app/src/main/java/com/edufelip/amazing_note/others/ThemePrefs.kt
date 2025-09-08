package com.edufelip.amazing_note.others

import android.content.Context
import androidx.core.content.edit

object ThemePrefs {
    private const val PREFS_NAME = "amazing_note_prefs"
    private const val KEY_DARK_MODE = "dark_mode"

    fun isDark(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DARK_MODE, false)

    fun setDark(context: Context, value: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putBoolean(KEY_DARK_MODE, value)
            }
    }
}

