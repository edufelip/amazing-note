package com.edufelip.shared.ui.effects.toast

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
actual fun rememberToastController(): ToastController {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidToastController(context) }
}

private class AndroidToastController(
    private val context: Context,
) : ToastController {
    override suspend fun show(message: ToastMessage) {
        withContext(Dispatchers.Main.immediate) {
            Toast.makeText(context, message.text, message.duration.toAndroidLength()).show()
        }
    }
}

private fun ToastDuration.toAndroidLength(): Int = when (this) {
    ToastDuration.Short -> Toast.LENGTH_SHORT
    ToastDuration.Long -> Toast.LENGTH_LONG
}
