package com.edufelip.shared.ui.util.platform

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private object AndroidPlatformChromeStrategy : PlatformChromeStrategy {
    override val defaultShowBottomBar: Boolean = true
    override val bottomBarHeight: Dp = 72.dp
    override val contentWindowInsets: WindowInsets = WindowInsets(0)

    override fun Modifier.applyTopBarStatusPadding(): Modifier = this
    override val topBarWindowInsets: WindowInsets = WindowInsets(0)

    override fun topBarContainerColor(defaultColor: Color): Color = defaultColor

    override fun Modifier.applyNavigationBarsPadding(): Modifier = navigationBarsPadding()
    override fun Modifier.applyAdditionalContentPadding(topBarVisible: Boolean): Modifier = this

    override fun calculateBottomPadding(
        isBottomBarEnabled: Boolean,
        bottomBarTargetVisible: Boolean,
        safeAreaPadding: PaddingValues,
        bottomBarHeight: Dp,
    ): Dp = if (isBottomBarEnabled && bottomBarTargetVisible) {
        bottomBarHeight
    } else {
        safeAreaPadding.calculateBottomPadding()
    }

    override val useCupertinoLook: Boolean = false
}

actual fun platformChromeStrategy(): PlatformChromeStrategy = AndroidPlatformChromeStrategy
