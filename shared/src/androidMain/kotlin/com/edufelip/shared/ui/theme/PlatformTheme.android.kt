package com.edufelip.shared.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberPlatformColorScheme(
    darkTheme: Boolean,
    useDynamicColor: Boolean,
): ColorScheme? {
    if (!useDynamicColor) return null
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val ctx = LocalContext.current
    return if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
}
