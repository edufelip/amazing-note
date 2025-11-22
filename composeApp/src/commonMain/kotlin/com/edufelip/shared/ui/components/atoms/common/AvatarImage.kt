package com.edufelip.shared.ui.components.atoms.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
expect fun AvatarImage(
    photoUrl: String?,
    size: Dp,
    modifier: Modifier,
)

@Composable
fun AvatarImage(
    photoUrl: String?,
    size: Dp,
) = AvatarImage(photoUrl, size, Modifier)
