package com.edufelip.shared.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edufelip.shared.auth.AuthController
import com.edufelip.shared.i18n.Str
import com.edufelip.shared.i18n.string

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
    LaunchedEffect(user) {
        if (user != null) {
            snackbarHostState.showSnackbar(string(Str.LoginSuccess))
            kotlinx.coroutines.delay(400)
            onBack()
        }
    }

    // snackbarHostState declared above

    LaunchedEffect(message) {
        when (message) {
            "RESET_EMAIL_SENT" -> {
                snackbarHostState.showSnackbar(string(Str.ResetEmailSent))
                auth.clearMessage()
            }
            "SIGN_UP_SUCCESS" -> {
                snackbarHostState.showSnackbar(string(Str.SignUpSuccess))
                auth.clearMessage()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = string(Str.LoginTitle))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(string(Str.Email)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(string(Str.Password)) },
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) CircularProgressIndicator()
                else Text(string(Str.Login))
            }

    val scope = rememberCoroutineScope()
    Button(
                onClick = {
                    onRequestGoogleSignIn?.invoke { success, err ->
                        if (!success) {
                            // Show Google sign-in error or cancellation
                            val msg = err ?: string(Str.GoogleSignInCanceled)
                            scope.launch { snackbarHostState.showSnackbar(msg) }
                        }
                    }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(string(Str.GoogleSignIn))
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = {
                if (email.isNotBlank()) auth.sendPasswordReset(email.trim())
            }, enabled = !loading) {
                Text(string(Str.ForgotPassword))
            }
            TextButton(onClick = {
                if (email.isNotBlank() && password.isNotBlank()) auth.signUp(email.trim(), password)
            }, enabled = !loading) {
                Text(string(Str.SignUp))
            }
        }
    }
}
