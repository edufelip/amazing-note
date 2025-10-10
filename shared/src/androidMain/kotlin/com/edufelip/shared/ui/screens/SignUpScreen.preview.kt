package com.edufelip.shared.ui.screens

import androidx.compose.runtime.Composable
import com.edufelip.shared.ui.nav.screens.SignUpScreen
import com.edufelip.shared.ui.preview.PreviewLocalized
import com.edufelip.shared.ui.preview.ScreenPreviewsDarkLight

@ScreenPreviewsDarkLight
@Composable
fun SignUpScreen_Previews() {
    PreviewLocalized {
        SignUpScreen(
            onBack = {},
            onSubmit = { _, _ -> },
            loading = false,
        )
    }
}
