package com.edufelip.shared.ui.features.auth.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edufelip.shared.data.auth.GoogleSignInLauncher
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_back
import com.edufelip.shared.resources.cd_hide_password
import com.edufelip.shared.resources.cd_show_password
import com.edufelip.shared.resources.continue_with_google
import com.edufelip.shared.resources.email
import com.edufelip.shared.resources.forgot_password
import com.edufelip.shared.resources.google_sign_in_canceled
import com.edufelip.shared.resources.ic_google
import com.edufelip.shared.resources.login
import com.edufelip.shared.resources.login_dont_have_account
import com.edufelip.shared.resources.login_headline
import com.edufelip.shared.resources.login_signup_cta
import com.edufelip.shared.resources.login_subheadline
import com.edufelip.shared.resources.login_title
import com.edufelip.shared.resources.password
import com.edufelip.shared.resources.reset_email_sent
import com.edufelip.shared.resources.sign_up_success
import com.edufelip.shared.ui.vm.AuthViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    auth: AuthViewModel,
    onBack: () -> Unit,
    googleSignInLauncher: GoogleSignInLauncher? = null,
    onOpenSignUp: () -> Unit,
    onLoginSuccess: () -> Unit = onBack,
    showLocalSuccessSnackbar: Boolean = true,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val loading by auth.loading.collectAsState()
    val error by auth.error.collectAsState()
    val user by auth.user.collectAsState()
    val message by auth.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var forgotPasswordDialogVisible by rememberSaveable { mutableStateOf(false) }
    var resetEmail by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(user) {
        if (user != null) {
            onLoginSuccess()
        }
    }

    val resetEmailSentText = stringResource(Res.string.reset_email_sent)
    val signUpSuccessText = stringResource(Res.string.sign_up_success)

    LaunchedEffect(message) {
        when (message) {
            "RESET_EMAIL_SENT" -> {
                forgotPasswordDialogVisible = false
                snackbarHostState.showSnackbar(resetEmailSentText)
                auth.clearMessage()
            }

            "SIGN_UP_SUCCESS" -> if (showLocalSuccessSnackbar) {
                snackbarHostState.showSnackbar(signUpSuccessText)
                auth.clearMessage()
            }
        }
    }

    LaunchedEffect(error) {
        val currentError = error
        if (currentError != null) {
            snackbarHostState.showSnackbar(currentError)
            auth.clearError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LoginHeader(onBack = onBack)
            Spacer(modifier = Modifier.height(32.dp))
            LoginIllustration(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f),
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(Res.string.login_headline),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(Res.string.login_subheadline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.email)) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.password)) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon: ImageVector
                    val description: String
                    if (passwordVisible) {
                        icon = Icons.Outlined.VisibilityOff
                        description = stringResource(Res.string.cd_hide_password)
                    } else {
                        icon = Icons.Outlined.Visibility
                        description = stringResource(Res.string.cd_show_password)
                    }
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = icon, contentDescription = description)
                    }
                },
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { auth.loginWithEmail(email.trim(), password) },
                enabled = email.isNotBlank() && password.isNotBlank() && !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(999.dp),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text(text = stringResource(Res.string.login))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            val googleCanceledText = stringResource(Res.string.google_sign_in_canceled)
            GoogleButton(
                text = stringResource(Res.string.continue_with_google),
                enabled = googleSignInLauncher != null && !loading,
                onClick = {
                    val launcher = googleSignInLauncher ?: return@GoogleButton
                    scope.launch {
                        val result = launcher.signIn()
                        when {
                            !result.idToken.isNullOrBlank() -> {
                                auth.signInWithGoogleToken(result.idToken)
                            }
                            !result.errorMessage.isNullOrBlank() -> {
                                auth.setError(result.errorMessage)
                                snackbarHostState.showSnackbar(result.errorMessage)
                            }
                            else -> snackbarHostState.showSnackbar(googleCanceledText)
                        }
                    }
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = {
                    resetEmail = email.trim()
                    forgotPasswordDialogVisible = true
                },
                enabled = !loading,
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    text = stringResource(Res.string.forgot_password),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.login_dont_have_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(
                    onClick = onOpenSignUp,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                ) {
                    Text(
                        text = stringResource(Res.string.login_signup_cta),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                }
            }
        }

        if (forgotPasswordDialogVisible) {
            ForgotPasswordDialog(
                email = resetEmail,
                loading = loading,
                onEmailChange = { resetEmail = it },
                onDismiss = { forgotPasswordDialogVisible = false },
                onSubmit = {
                    val trimmed = resetEmail.trim()
                    resetEmail = trimmed
                    if (trimmed.isNotEmpty()) {
                        auth.sendPasswordReset(trimmed)
                    }
                },
            )
        }
    }
}

@Composable
private fun LoginHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(Res.string.cd_back),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = stringResource(Res.string.login_title),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun LoginIllustration(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(primary.copy(alpha = 0.12f)),
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (-36).dp, y = 64.dp)
                .background(primary.copy(alpha = 0.35f), CircleShape),
        )
        Box(
            modifier = Modifier
                .size(88.dp)
                .offset(x = 90.dp, y = (-24).dp)
                .background(tertiary.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
        )
        Box(
            modifier = Modifier
                .size(52.dp)
                .offset(x = 180.dp, y = 48.dp)
                .background(primary.copy(alpha = 0.3f), CircleShape),
        )
        Icon(
            imageVector = Icons.Outlined.DarkMode,
            contentDescription = null,
            tint = primary,
            modifier = Modifier
                .align(Alignment.Center)
                .size(56.dp)
                .rotate(45f),
        )
    }
}

@Composable
private fun ForgotPasswordDialog(
    email: String,
    onEmailChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
    loading: Boolean,
) {
    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        title = { Text(text = stringResource(Res.string.forgot_password)) },
        text = {
            Column {
                Text(
                    text = "Enter your email to receive a reset link.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.email)) },
                    singleLine = true,
                    enabled = !loading,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSubmit,
                enabled = !loading && email.isNotBlank(),
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(text = "Send reset email")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !loading,
            ) {
                Text(text = "Cancel")
            }
        },
    )
}

@Composable
private fun GoogleButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF3C4043),
            disabledContainerColor = Color.White.copy(alpha = 0.6f),
            disabledContentColor = Color(0xFF3C4043).copy(alpha = 0.6f),
        ),
        contentPadding = PaddingValues(horizontal = 16.dp),
        enabled = enabled,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_google),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 12.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFF3C4043),
            )
        }
    }
}
