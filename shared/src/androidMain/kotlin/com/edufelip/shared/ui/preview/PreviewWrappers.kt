package com.edufelip.shared.ui.preview

import androidx.compose.runtime.Composable
import com.edufelip.shared.ui.theme.AmazingNoteTheme

@Composable
fun PreviewLocalized(content: @Composable () -> Unit) {
    AmazingNoteTheme {
        content()
    }
}
