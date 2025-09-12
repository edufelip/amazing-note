package com.edufelip.shared.ui.web

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import java.net.URLEncoder

private fun resolveUrlForWebView(original: String): String = if (original.endsWith(".pdf", ignoreCase = true)) {
    val encoded = URLEncoder.encode(original, Charsets.UTF_8.name())
    "https://docs.google.com/gview?embedded=1&url=$encoded"
} else {
    original
}

@Composable
actual fun WebView(url: String, modifier: Modifier) {
    val finalUrl = resolveUrlForWebView(url)
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                webViewClient = WebViewClient()
                loadUrl(finalUrl)
            }
        },
        update = { it.loadUrl(finalUrl) },
    )
}
