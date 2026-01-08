package com.edufelip.shared.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private class AndroidNetworkStatus(
    context: Context,
) : NetworkStatus {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _isOnline = MutableStateFlow(isOnline(connectivityManager))
    override val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isOnline.value = true
        }

        override fun onLost(network: Network) {
            _isOnline.value = isOnline(connectivityManager)
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            _isOnline.value = isOnline(connectivityManager)
        }
    }

    init {
        val request = NetworkRequest.Builder().build()
        runCatching {
            connectivityManager.registerDefaultNetworkCallback(callback)
        }.getOrElse {
            connectivityManager.registerNetworkCallback(request, callback)
        }
    }

    fun dispose() {
        runCatching { connectivityManager.unregisterNetworkCallback(callback) }
    }

    private fun isOnline(connectivityManager: ConnectivityManager): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

@Composable
actual fun rememberNetworkStatus(): NetworkStatus {
    val context = LocalContext.current.applicationContext
    val status = remember(context) { AndroidNetworkStatus(context) }
    DisposableEffect(status) {
        onDispose { status.dispose() }
    }
    return status
}
