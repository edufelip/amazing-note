package com.edufelip.shared.ui.features.trash.screens

import com.edufelip.shared.preview.PreviewParameterProvider

internal actual class TrashScreenPreviewProvider actual constructor() : PreviewParameterProvider<TrashPreviewState> {
    actual override val values: Sequence<TrashPreviewState> = TrashPreviewSamples.states.asSequence()
}
