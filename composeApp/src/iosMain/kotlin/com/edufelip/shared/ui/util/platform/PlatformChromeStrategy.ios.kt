package com.edufelip.shared.ui.util.platform

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import platform.UIKit.UIDevice

private val supportsLiquidGlassInternal: Boolean by lazy {
    val major = UIDevice.currentDevice.systemVersion
        .substringBefore('.')
        .toIntOrNull() ?: 0
    major >= 18
}

private object IosPlatformChromeStrategy : PlatformChromeStrategy {
    override val defaultShowBottomBar: Boolean = false
    override fun topBarContainerColor(defaultColor: Color): Color = Color.Transparent
    override fun Modifier.applyNavigationBarsPadding(): Modifier = this
    override val useCupertinoLook: Boolean = supportsLiquidGlassInternal
}

actual fun platformChromeStrategy(): PlatformChromeStrategy = IosPlatformChromeStrategy
