package com.edufelip.shared.ui.editor

import androidx.compose.ui.Modifier

// Transferable content APIs are not yet available on iOS; no-op to keep parity with expect.
actual fun Modifier.noteEditorReceiveContent(onImage: (String) -> Unit): Modifier = this
