package com.edufelip.shared.ui.util.platform

import platform.UIKit.UIDevice

private val supportsLiquidGlassInternal: Boolean by lazy {
    val major = UIDevice.currentDevice.systemVersion
        .substringBefore('.')
        .toIntOrNull() ?: 0
    major >= 18
}

actual object PlatformFlags {
    actual val cupertinoLookEnabled: Boolean = supportsLiquidGlassInternal
    actual val isIosPlatform: Boolean = true
    actual val isIos: Boolean = true
    actual val supportsLiquidGlass: Boolean = supportsLiquidGlassInternal
}
