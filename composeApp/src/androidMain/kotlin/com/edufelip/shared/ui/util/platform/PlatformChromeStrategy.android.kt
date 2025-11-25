package com.edufelip.shared.ui.util.platform

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private object AndroidPlatformChromeStrategy : PlatformChromeStrategy {
    override val defaultShowBottomBar: Boolean = true

    override fun topBarContainerColor(defaultColor: Color): Color = defaultColor

    override fun Modifier.applyNavigationBarsPadding(): Modifier = navigationBarsPadding()

    override val useCupertinoLook: Boolean = false
}

actual fun platformChromeStrategy(): PlatformChromeStrategy = AndroidPlatformChromeStrategy
