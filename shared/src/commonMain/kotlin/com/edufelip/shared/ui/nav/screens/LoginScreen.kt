package com.edufelip.shared.ui.nav.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edufelip.shared.presentation.AuthViewModel
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.continue_with_google
import com.edufelip.shared.resources.email
import com.edufelip.shared.resources.forgot_password
import com.edufelip.shared.resources.google_sign_in_canceled
import com.edufelip.shared.resources.ic_google
import com.edufelip.shared.resources.login
import com.edufelip.shared.resources.login_success
import com.edufelip.shared.resources.login_title
import com.edufelip.shared.resources.password
import com.edufelip.shared.resources.reset_email_sent
import com.edufelip.shared.resources.sign_up
import com.edufelip.shared.resources.sign_up_success
import com.edufelip.shared.resources.cd_back
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    auth: AuthViewModel,
    onBack: () -> Unit,
    onRequestGoogleSignIn: (((Boolean, String?) -> Unit) -> Unit)? = null,
    onOpenSignUp: () -> Unit,
    onLoginSuccess: () -> Unit = onBack,
    showLocalSuccessSnackbar: Boolean = true,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loading by auth.loading.collectAsState()
    val error by auth.error.collectAsState()
    val user by auth.user.collectAsState()
    val message by auth.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val loginSuccessText = stringResource(Res.string.login_success)

    LaunchedEffect(user) {
        if (user != null) {
            if (showLocalSuccessSnackbar) {
                snackbarHostState.showSnackbar(loginSuccessText)
                delay(400)
            }
            onLoginSuccess()
        }
    }

    val resetEmailSentText = stringResource(Res.string.reset_email_sent)
    val signUpSuccessText = stringResource(Res.string.sign_up_success)

    LaunchedEffect(message) {
        when (message) {
            "RESET_EMAIL_SENT" -> {
                snackbarHostState.showSnackbar(resetEmailSentText)
                auth.clearMessage()
            }

            "SIGN_UP_SUCCESS" -> {
                snackbarHostState.showSnackbar(signUpSuccessText)
                auth.clearMessage()
            }
        }
    }

    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(error ?: "")
            auth.clearError()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.cd_back),
                            tint = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(Res.string.login_title),
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(Res.string.email)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(Res.string.password)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { auth.loginWithEmail(email.trim(), password) },
                enabled = email.isNotBlank() && password.isNotBlank() && !loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (loading) {
                    CircularProgressIndicator()
                } else {
                    Text(stringResource(Res.string.login))
                }
            }
            Spacer(Modifier.height(12.dp))

            val scope = rememberCoroutineScope()
            val googleCanceledText = stringResource(Res.string.google_sign_in_canceled)
                Button(
                onClick = {
                    onRequestGoogleSignIn?.invoke { success, err ->
                        if (!success) {
                            val msg = err ?: googleCanceledText
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF3C4043),
                ),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFFDADCE0)),
                contentPadding = PaddingValues(0.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Box(Modifier.width(48.dp)) {
                        Image(
                            painter = painterResource(Res.drawable.ic_google),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .padding(start = 12.dp),
                        )
                    }
                    Text(
                        stringResource(Res.string.continue_with_google),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF3C4043),
                        ),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    if (email.isNotBlank()) auth.sendPasswordReset(email.trim())
                },
                enabled = !loading
            ) {
                Box(Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(Res.string.forgot_password),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onOpenSignUp()
                }
            ) {
                Box(Modifier.fillMaxWidth()) {
                    Text(
                        stringResource(Res.string.sign_up),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
