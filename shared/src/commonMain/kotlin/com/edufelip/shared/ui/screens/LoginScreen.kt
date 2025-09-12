package com.edufelip.shared.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edufelip.shared.auth.AuthController
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.email
import com.edufelip.shared.resources.forgot_password
import com.edufelip.shared.resources.google_sign_in
import com.edufelip.shared.resources.google_sign_in_canceled
import com.edufelip.shared.resources.login
import com.edufelip.shared.resources.login_success
import com.edufelip.shared.resources.login_title
import com.edufelip.shared.resources.password
import com.edufelip.shared.resources.reset_email_sent
import com.edufelip.shared.resources.sign_up
import com.edufelip.shared.resources.sign_up_success
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    auth: AuthController,
    onBack: () -> Unit,
    onRequestGoogleSignIn: (((Boolean, String?) -> Unit) -> Unit)? = null,
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
            snackbarHostState.showSnackbar(loginSuccessText)
            kotlinx.coroutines.delay(400)
            onBack()
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = stringResource(Res.string.login_title))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(Res.string.email)) },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(Res.string.password)) },
                modifier = Modifier.fillMaxWidth(),
            )

            LaunchedEffect(error) {
                if (error != null) {
                    snackbarHostState.showSnackbar(error ?: "")
                    auth.clearError()
                }
            }

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

            val scope = rememberCoroutineScope()
            val googleCanceledText = stringResource(Res.string.google_sign_in_canceled)
            Button(
                onClick = {
                    onRequestGoogleSignIn?.invoke { success, err ->
                        if (!success) {
                            // Show Google sign-in error or cancellation
                            val msg = err ?: googleCanceledText
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.google_sign_in))
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = {
                if (email.isNotBlank()) auth.sendPasswordReset(email.trim())
            }, enabled = !loading) {
                Text(stringResource(Res.string.forgot_password))
            }
            TextButton(onClick = {
                if (email.isNotBlank() && password.isNotBlank()) auth.signUp(email.trim(), password)
            }, enabled = !loading) {
                Text(stringResource(Res.string.sign_up))
            }
        }
    }
}
