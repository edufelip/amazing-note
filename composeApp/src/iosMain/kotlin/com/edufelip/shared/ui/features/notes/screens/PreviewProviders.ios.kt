package com.edufelip.shared.ui.features.notes.screens

import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

internal actual class AddNoteScreenPreviewProvider actual constructor() : PreviewParameterProvider<AddNoteScreenPreviewState> {
    actual override val values: Sequence<AddNoteScreenPreviewState> = AddNotePreviewSamples.states.asSequence()
}

internal actual class FoldersScreenPreviewProvider actual constructor() : PreviewParameterProvider<FoldersPreviewState> {
    actual override val values: Sequence<FoldersPreviewState> = FoldersPreviewSamples.states.asSequence()
}
