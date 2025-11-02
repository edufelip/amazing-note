package com.edufelip.shared.ui.web

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun WebView(url: String, modifier: Modifier = Modifier)
