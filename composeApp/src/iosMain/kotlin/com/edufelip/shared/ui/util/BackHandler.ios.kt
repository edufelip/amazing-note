package com.edufelip.shared.ui.util

import androidx.compose.runtime.Composable

@Composable
actual fun OnSystemBack(onBack: () -> Unit) {
    // iOS does not have a system back button in this hosting context.
}
