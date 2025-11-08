package com.edufelip.shared.ui.settings

import platform.Foundation.NSUserDefaults

object IosSettings : Settings {
    private val defaults = NSUserDefaults.standardUserDefaults()

    override fun getBool(key: String, default: Boolean): Boolean {
        val obj = defaults.objectForKey(key)
        return obj?.let { defaults.boolForKey(key) } ?: default
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
