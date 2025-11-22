package com.edufelip.shared.ui.settings

import androidx.compose.runtime.staticCompositionLocalOf

interface Settings {
    fun getBool(key: String, default: Boolean): Boolean
    fun setBool(key: String, value: Boolean)
    fun getString(key: String, default: String): String
    fun setString(key: String, value: String)
}

class InMemorySettings : Settings {
    private val bools = mutableMapOf<String, Boolean>()
    private val strings = mutableMapOf<String, String>()
    override fun getBool(key: String, default: Boolean): Boolean = bools[key] ?: default
    override fun setBool(key: String, value: Boolean) {
        bools[key] = value
    }
    override fun getString(key: String, default: String): String = strings[key] ?: default
    override fun setString(key: String, value: String) {
        strings[key] = value
    }
}

val LocalSettings = staticCompositionLocalOf<Settings> { InMemorySettings() }
