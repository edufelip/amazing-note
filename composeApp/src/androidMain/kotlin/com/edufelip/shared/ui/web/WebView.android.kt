package com.edufelip.shared.ui.web

import android.graphics.Bitmap
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
    var isLoading by remember(finalUrl) { mutableStateOf(true) }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            isLoading = true
                        }

                        override fun onPageCommitVisible(view: WebView?, url: String?) {
                            isLoading = false
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            isLoading = false
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: android.webkit.WebResourceRequest?,
                            error: android.webkit.WebResourceError?,
                        ) {
                            isLoading = false
                        }
                    }
                    loadUrl(finalUrl)
                }
            },
            update = {
                isLoading = true
                it.loadUrl(finalUrl)
            },
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth,
            )
        }
    }
}
