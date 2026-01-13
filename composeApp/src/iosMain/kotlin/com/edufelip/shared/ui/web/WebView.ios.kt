@file:OptIn(ExperimentalForeignApi::class)

package com.edufelip.shared.ui.web

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
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKNavigation
import platform.WebKit.WKNavigationDelegateProtocol
import platform.WebKit.WKWebView
import platform.darwin.NSObject

@Composable
actual fun WebView(url: String, modifier: Modifier) {
    var isLoading by remember(url) { mutableStateOf(true) }
    val delegate = remember { ComposeNavigationDelegate(onFinished = { isLoading = false }) { isLoading = true } }

    Box(modifier = modifier) {
        UIKitView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                WKWebView().apply {
                    navigationDelegate = delegate
                    val nsUrl = NSURL.URLWithString(url)
                    if (nsUrl != null) {
                        loadRequest(NSURLRequest.requestWithURL(nsUrl))
                    }
                }
            },
            update = { webView ->
                webView.navigationDelegate = delegate
                val nsUrl = NSURL.URLWithString(url)
                if (nsUrl != null) {
                    isLoading = true
                    webView.loadRequest(NSURLRequest.requestWithURL(nsUrl))
                }
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

private class ComposeNavigationDelegate(
    private val onFinished: () -> Unit,
    private val onStarted: () -> Unit,
) : NSObject(),
    WKNavigationDelegateProtocol {
    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didStartProvisionalNavigation: WKNavigation?) {
        onStarted()
    }

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFinishNavigation: WKNavigation?) {
        onFinished()
    }

    @ObjCSignatureOverride
    override fun webView(
        webView: WKWebView,
        didFailProvisionalNavigation: WKNavigation?,
        withError: NSError,
    ) {
        onFinished()
    }

    @ObjCSignatureOverride
    override fun webView(webView: WKWebView, didFailNavigation: WKNavigation?, withError: NSError) {
        onFinished()
    }

    @ObjCSignatureOverride
    override fun webViewWebContentProcessDidTerminate(webView: WKWebView) {
        onFinished()
    }
}
