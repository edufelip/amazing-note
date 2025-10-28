package com.edufelip.shared.ui.nav.screens

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

internal actual class AddNoteScreenPreviewProvider actual constructor() : PreviewParameterProvider<AddNoteScreenPreviewState> {
    actual override val values: Sequence<AddNoteScreenPreviewState> = AddNotePreviewSamples.states.asSequence()
}

internal actual class FoldersScreenPreviewProvider actual constructor() : PreviewParameterProvider<FoldersPreviewState> {
    actual override val values: Sequence<FoldersPreviewState> = FoldersPreviewSamples.states.asSequence()
}

internal actual class HomeScreenPreviewProvider actual constructor() : PreviewParameterProvider<HomePreviewState> {
    actual override val values: Sequence<HomePreviewState> = HomePreviewSamples.states.asSequence()
}

internal actual class ListScreenPreviewProvider actual constructor() : PreviewParameterProvider<ListPreviewState> {
    actual override val values: Sequence<ListPreviewState> = ListPreviewSamples.states.asSequence()
}

internal actual class TrashScreenPreviewProvider actual constructor() : PreviewParameterProvider<TrashPreviewState> {
    actual override val values: Sequence<TrashPreviewState> = TrashPreviewSamples.states.asSequence()
}

internal actual class SignUpScreenPreviewProvider actual constructor() : PreviewParameterProvider<SignUpPreviewState> {
    actual override val values: Sequence<SignUpPreviewState> = SignUpPreviewSamples.states.asSequence()
}
