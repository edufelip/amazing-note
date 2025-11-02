package com.edufelip.shared.ui.util.platform

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

interface PlatformChromeStrategy {
    val defaultShowBottomBar: Boolean
    val bottomBarHeight: Dp
    val contentWindowInsets: WindowInsets

    fun Modifier.applyTopBarStatusPadding(): Modifier
    val topBarWindowInsets: WindowInsets
    fun topBarContainerColor(defaultColor: Color): Color

    fun Modifier.applyNavigationBarsPadding(): Modifier
    fun Modifier.applyAdditionalContentPadding(topBarVisible: Boolean): Modifier

    fun calculateBottomPadding(
        isBottomBarEnabled: Boolean,
        bottomBarTargetVisible: Boolean,
        safeAreaPadding: PaddingValues,
        bottomBarHeight: Dp,
    ): Dp

    val useCupertinoLook: Boolean
}

expect fun platformChromeStrategy(): PlatformChromeStrategy
