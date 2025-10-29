package com.edufelip.shared.ui.features.notes.components

import com.edufelip.shared.preview.PreviewParameterProvider

internal actual class ListScreenPreviewProvider actual constructor() : PreviewParameterProvider<ListPreviewState> {
    actual override val values: Sequence<ListPreviewState> = ListPreviewSamples.states.asSequence()
}
