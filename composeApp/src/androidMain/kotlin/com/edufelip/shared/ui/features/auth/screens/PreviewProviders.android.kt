@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_BETA")

package com.edufelip.shared.ui.features.auth.screens

import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

internal actual class SignUpScreenPreviewProvider actual constructor() : PreviewParameterProvider<SignUpPreviewState> {
    actual override val values: Sequence<SignUpPreviewState> = SignUpPreviewSamples.states.asSequence()
}
