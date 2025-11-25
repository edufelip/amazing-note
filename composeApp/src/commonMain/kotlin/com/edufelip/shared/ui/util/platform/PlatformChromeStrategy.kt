package com.edufelip.shared.ui.util.platform

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

interface PlatformChromeStrategy {
    val defaultShowBottomBar: Boolean
    fun topBarContainerColor(defaultColor: Color): Color
    fun Modifier.applyNavigationBarsPadding(): Modifier
    val useCupertinoLook: Boolean
}

expect fun platformChromeStrategy(): PlatformChromeStrategy
