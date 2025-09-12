package com.edufelip.amazing_note.others

import android.content.Context
import androidx.core.content.edit
import com.edufelip.shared.ui.settings.Settings

class AndroidSettings(context: Context) : Settings {
    private val prefs = context.getSharedPreferences("amazing_note_prefs", Context.MODE_PRIVATE)
    override fun getBool(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)
    override fun setBool(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }
    override fun getString(key: String, default: String): String = prefs.getString(key, default) ?: default
    override fun setString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }
}
