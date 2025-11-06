package com.edufelip.shared.ui.app.chrome

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.edufelip.shared.ui.app.state.AmazingNoteAppState
import com.edufelip.shared.ui.nav.AppRoutes

@Composable
expect fun AmazingNoteScaffold(
    state: AmazingNoteAppState,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit,
    onTabSelected: (AppRoutes) -> Unit,
    content: @Composable (PaddingValues, Dp) -> Unit,
)
