package com.edufelip.shared.ui.ios

import androidx.compose.runtime.Composable

@Composable
expect fun IosDatePicker(
    epochMillis: Long,
    onChange: (Long) -> Unit,
)
