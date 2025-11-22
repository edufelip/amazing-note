package com.edufelip.shared.ui.features.settings.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.privacy_policy
import com.edufelip.shared.ui.preview.DevicePreviews
import com.edufelip.shared.ui.util.Constants
import com.edufelip.shared.ui.web.WebView
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    url: String = Constants.PRIVACY_POLICY_URL,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    val isInPreview = LocalInspectionMode.current
    Scaffold(
        topBar = {
            Surface(tonalElevation = 2.dp, shadowElevation = 1.dp) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(Res.string.privacy_policy),
                            style = MaterialTheme.typography.titleSmall,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
            }
        },
    ) { padding ->
        if (!isInPreview) {
            Box(modifier = Modifier.padding(padding)) {
                WebView(
                    url = url,
                    modifier = modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Preview
@DevicePreviews
@Composable
private fun PrivacyPolicyPreview() {
    PrivacyScreen(
        onBack = {},
    )
}
