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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edufelip.shared.Constants
import com.edufelip.shared.auth.AuthController
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
    auth: AuthController?,
    onOpenLogin: () -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToTrash: () -> Unit,
    onLogout: () -> Unit,
) {
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
                userName = auth?.user?.value?.displayName,
                userEmail = auth?.user?.value?.email,
                userPhotoUrl = auth?.user?.value?.photoUrl,
                onLoginClick = onOpenLogin,
                onLogoutClick = onLogout,
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
