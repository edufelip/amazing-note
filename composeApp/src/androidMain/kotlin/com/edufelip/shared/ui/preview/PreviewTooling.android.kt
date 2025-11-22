@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_BETA")

package com.edufelip.shared.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.annotation.AnnotationRetention
import kotlin.annotation.AnnotationTarget
import kotlin.annotation.Retention
import kotlin.annotation.Target

@Composable
internal actual fun ProvidePreviewConfiguration(
    localized: Boolean,
    content: @Composable () -> Unit,
) {
    if (localized) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            content()
        }
    } else {
        content()
    }
}

@Preview(name = "Phone", group = "Devices")
@Preview(name = "Tablet", group = "Devices")
@Preview(name = "Desktop", group = "Devices")
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
actual annotation class DevicePreviews
