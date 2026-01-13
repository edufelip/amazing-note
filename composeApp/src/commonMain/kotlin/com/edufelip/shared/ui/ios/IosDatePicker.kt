package com.edufelip.shared.ui.ios

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun IosDatePicker(
    epochMillis: Long,
    onChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
)
