package com.edufelip.shared.ui.util

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
actual fun OnSystemBack(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
}
