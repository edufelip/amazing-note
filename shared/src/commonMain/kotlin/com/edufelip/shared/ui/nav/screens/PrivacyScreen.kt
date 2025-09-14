package com.edufelip.shared.ui.nav.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.edufelip.shared.Constants
import com.edufelip.shared.presentation.AuthViewModel
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.privacy_policy
import com.edufelip.shared.ui.gadgets.DrawerContent
import com.edufelip.shared.ui.web.WebView
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    url: String = Constants.PRIVACY_POLICY_URL,
    modifier: Modifier = Modifier,
    drawerState: DrawerState,
    darkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    auth: AuthViewModel?,
    onOpenLogin: () -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onLogout: () -> Unit,
) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val currentUserForDrawer = if (auth != null) auth.user.collectAsState().value else null

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                onYourNotesClick = onNavigateToHome,
                onTrashClick = onNavigateToTrash,
                darkTheme = darkTheme,
                onToggleDarkTheme = onToggleDarkTheme,
                selectedHome = false,
                selectedTrash = false,
                onPrivacyClick = onOpenDrawer,
                userName = currentUserForDrawer?.displayName,
                userEmail = currentUserForDrawer?.email,
                userPhotoUrl = currentUserForDrawer?.photoUrl,
                onLoginClick = onOpenLogin,
                onLogoutClick = {
                    scope.launch { drawerState.close() }
                    onLogout()
                },
            )
        },
    ) {
        Scaffold(
            topBar = {
                Surface(tonalElevation = 2.dp, shadowElevation = 1.dp) {
                    TopAppBar(
                        title = { androidx.compose.material3.Text(text = stringResource(Res.string.privacy_policy)) },
                        navigationIcon = {
                            IconButton(onClick = onOpenDrawer) {
                                Icon(imageVector = Icons.Default.Menu, contentDescription = null)
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
}
