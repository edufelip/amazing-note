package com.edufelip.shared.ui.theme

import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
internal actual fun ApplySystemBarStyle(isDarkTheme: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    val activity = view.context as? ComponentActivity ?: return
    val window = activity.window

    SideEffect {
        val controller = WindowCompat.getInsetsController(window, view) ?: return@SideEffect
        // Light icons on dark backgrounds; dark icons on light backgrounds.
        controller.isAppearanceLightStatusBars = !isDarkTheme
        controller.isAppearanceLightNavigationBars = !isDarkTheme
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }
}
