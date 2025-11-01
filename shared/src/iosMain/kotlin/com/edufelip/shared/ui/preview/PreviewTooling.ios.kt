package com.edufelip.shared.ui.preview

import androidx.compose.runtime.Composable
import kotlin.annotation.AnnotationRetention
import kotlin.annotation.AnnotationTarget
import kotlin.annotation.Retention
import kotlin.annotation.Target

@Composable
internal actual fun ProvidePreviewConfiguration(
    localized: Boolean,
    content: @Composable () -> Unit,
) {
    content()
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
actual annotation class DevicePreviews
