package com.edufelip.shared.ui.gadgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
expect fun AvatarImage(
    photoUrl: String?,
    size: Dp,
    modifier: Modifier = Modifier,
)
