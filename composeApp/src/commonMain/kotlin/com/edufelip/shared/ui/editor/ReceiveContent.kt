package com.edufelip.shared.ui.editor

import androidx.compose.ui.Modifier

expect fun Modifier.noteEditorReceiveContent(onImage: (String) -> Unit): Modifier
