package com.edufelip.shared.data.network

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.StateFlow

interface NetworkStatus {
    val isOnline: StateFlow<Boolean>
}

@Composable
expect fun rememberNetworkStatus(): NetworkStatus

val LocalNetworkStatus = staticCompositionLocalOf<NetworkStatus?> { null }
