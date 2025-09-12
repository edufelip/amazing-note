package com.edufelip.shared.ui.routes

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edufelip.shared.Constants
import com.edufelip.shared.ui.web.WebView

@Composable
fun PrivacyRoute(
    url: String = Constants.PRIVACY_POLICY_URL,
    modifier: Modifier = Modifier,
) {
    WebView(
        url = url,
        modifier = modifier.fillMaxSize(),
    )
}
