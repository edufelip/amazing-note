package com.edufelip.shared.ui.features.auth.screens

import com.edufelip.shared.preview.PreviewParameterProvider

internal actual class SignUpScreenPreviewProvider actual constructor() : PreviewParameterProvider<SignUpPreviewState> {
    actual override val values: Sequence<SignUpPreviewState> = SignUpPreviewSamples.states.asSequence()
}
