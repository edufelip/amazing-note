package com.edufelip.shared.ui.features.trash.screens

import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

internal actual class TrashScreenPreviewProvider actual constructor() : PreviewParameterProvider<TrashPreviewState> {
    actual override val values: Sequence<TrashPreviewState> = TrashPreviewSamples.states.asSequence()
}
