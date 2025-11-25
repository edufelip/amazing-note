package com.edufelip.shared.ui.ios

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun IosDatePicker(
    epochMillis: Long,
    onChange: (Long) -> Unit,
    modifier: Modifier,
) {
    // Android does not render the Cupertino picker; callers should provide an alternative.
}
