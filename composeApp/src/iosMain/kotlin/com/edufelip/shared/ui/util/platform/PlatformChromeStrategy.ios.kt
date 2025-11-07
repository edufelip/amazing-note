package com.edufelip.shared.ui.util.platform

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import platform.UIKit.UIDevice

private val supportsLiquidGlassInternal: Boolean by lazy {
    val major = UIDevice.currentDevice.systemVersion
        .substringBefore('.')
        .toIntOrNull() ?: 0
    major >= 18
}

private object IosPlatformChromeStrategy : PlatformChromeStrategy {
    override val defaultShowBottomBar: Boolean = false
    override val bottomBarHeight: Dp = 0.dp
    override val contentWindowInsets: WindowInsets = WindowInsets(0)

    override fun Modifier.applyTopBarStatusPadding(): Modifier = statusBarsPadding()
    override val topBarWindowInsets: WindowInsets = WindowInsets(0)

    override fun topBarContainerColor(defaultColor: Color): Color = Color.Transparent

    override fun Modifier.applyNavigationBarsPadding(): Modifier = this
    override fun Modifier.applyAdditionalContentPadding(topBarVisible: Boolean): Modifier = this

    @Composable
    override fun navigationBarBottomInset(): Dp = 0.dp

    override fun calculateBottomPadding(
        isBottomBarEnabled: Boolean,
        bottomBarTargetVisible: Boolean,
        safeAreaPadding: PaddingValues,
        bottomBarHeight: Dp,
    ): Dp = 0.dp

    override val useCupertinoLook: Boolean = supportsLiquidGlassInternal
}

actual fun platformChromeStrategy(): PlatformChromeStrategy = IosPlatformChromeStrategy
