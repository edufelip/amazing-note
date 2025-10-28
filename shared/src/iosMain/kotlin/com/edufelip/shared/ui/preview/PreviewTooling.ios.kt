package com.edufelip.shared.ui.preview

import androidx.compose.runtime.Composable

@Composable
internal actual fun ProvidePreviewConfiguration(
    localized: Boolean,
    content: @Composable () -> Unit,
) {
    content()
}

actual annotation class DevicePreviews
