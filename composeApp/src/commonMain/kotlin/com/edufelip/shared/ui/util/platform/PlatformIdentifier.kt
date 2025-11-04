package com.edufelip.shared.ui.util.platform

/**
 * Returns `true` when running on an Apple platform (iOS/macOS), `false` otherwise.
 * Useful for applying workarounds for Compose bugs that manifest only on Apple targets.
 */
expect fun isApplePlatform(): Boolean
