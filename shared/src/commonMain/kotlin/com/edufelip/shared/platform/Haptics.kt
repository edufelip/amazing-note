package com.edufelip.shared.platform

/**
 * Lightweight haptic feedback helpers so we can provide subtle taps on iOS
 * without affecting Android behaviour.
 */
expect object Haptics {
    fun lightTap()
}
