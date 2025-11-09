package com.edufelip.shared.ui.effects.toast

import androidx.compose.runtime.Composable

enum class ToastDuration { Short, Long }

data class ToastMessage(
    val text: String,
    val duration: ToastDuration = ToastDuration.Short,
)

interface ToastController {
    suspend fun show(message: ToastMessage)
}

suspend fun ToastController.show(
    text: String,
    duration: ToastDuration = ToastDuration.Short,
) = show(ToastMessage(text = text, duration = duration))

@Composable
expect fun rememberToastController(): ToastController
