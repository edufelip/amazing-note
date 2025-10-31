package com.edufelip.shared.ui.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.unit.LayoutDirection
import com.edufelip.shared.preview.Preview

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

@Preview(
    name = "Phone",
    group = "Devices",
    device = Devices.PHONE,
    showBackground = true,
    showSystemUi = true,
)
@Preview(
    name = "Tablet",
    group = "Devices",
    device = Devices.TABLET,
    showBackground = true,
    showSystemUi = true,
    fontScale = 1.3f,
)
@Preview(
    name = "Desktop",
    group = "Devices",
    device = "spec:width=1280dp,height=720dp,dpi=240",
    showBackground = true,
    showSystemUi = true,
)
actual annotation class DevicePreviews
