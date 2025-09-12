package com.edufelip.shared.ui.gadgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun AvatarImage(photoUrl: String?, modifier: Modifier = Modifier)
