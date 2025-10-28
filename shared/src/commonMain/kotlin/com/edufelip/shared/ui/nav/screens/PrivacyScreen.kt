package com.edufelip.shared.ui.nav.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.privacy_policy
import com.edufelip.shared.ui.util.Constants
import com.edufelip.shared.ui.web.WebView
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    url: String = Constants.PRIVACY_POLICY_URL,
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            Surface(tonalElevation = 2.dp, shadowElevation = 1.dp) {
                TopAppBar(
                    title = { androidx.compose.material3.Text(text = stringResource(Res.string.privacy_policy)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer,
                        navigationIconContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer,
                        titleContentColor = androidx.compose.material3.MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            WebView(
                url = url,
                modifier = modifier.fillMaxSize(),
            )
        }
    }
}
