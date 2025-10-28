package com.edufelip.shared.ui.features.auth.screens

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

internal actual class SignUpScreenPreviewProvider actual constructor() : PreviewParameterProvider<SignUpPreviewState> {
    actual override val values: Sequence<SignUpPreviewState> = SignUpPreviewSamples.states.asSequence()
}
