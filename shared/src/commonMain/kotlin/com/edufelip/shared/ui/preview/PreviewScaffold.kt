package com.edufelip.shared.ui.preview

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edufelip.shared.ui.theme.AmazingNoteTheme

@Composable
fun DevicePreviewContainer(
    isDarkTheme: Boolean = false,
    localized: Boolean = false,
    content: @Composable () -> Unit,
) {
    ProvidePreviewConfiguration(localized = localized) {
        AmazingNoteTheme(darkTheme = isDarkTheme) {
            Surface(
                modifier = Modifier.fillMaxSize(),
            ) {
                content()
            }
        }
    }
}

@Composable
internal expect fun ProvidePreviewConfiguration(
    localized: Boolean,
    content: @Composable () -> Unit,
)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
expect annotation class DevicePreviews()
