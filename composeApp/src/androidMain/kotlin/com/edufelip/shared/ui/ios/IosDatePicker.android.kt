package com.edufelip.shared.ui.ios

import androidx.compose.runtime.Composable

@Composable
actual fun IosDatePicker(
    epochMillis: Long,
    onChange: (Long) -> Unit,
) {
    // Android does not render the Cupertino picker; callers should provide an alternative.
}
