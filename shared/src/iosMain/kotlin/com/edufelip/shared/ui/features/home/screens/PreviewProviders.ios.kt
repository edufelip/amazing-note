package com.edufelip.shared.ui.features.home.screens

import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

internal actual class HomeScreenPreviewProvider actual constructor() : PreviewParameterProvider<HomePreviewState> {
    actual override val values: Sequence<HomePreviewState> = HomePreviewSamples.states.asSequence()
}
