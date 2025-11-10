package com.edufelip.shared.ui.features.auth.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_hide_password
import com.edufelip.shared.resources.cd_show_password
import com.edufelip.shared.resources.email
import com.edufelip.shared.resources.login
import com.edufelip.shared.resources.login_error_invalid_credentials
import com.edufelip.shared.resources.password
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun LoginCredentialsSection(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    loading: Boolean,
    onSubmit: () -> Unit,
    showError: Boolean,
    errorMessage: String,
    modifier: Modifier = Modifier,
) {
    val tokens = designTokens()
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(stringResource(Res.string.email)) },
        singleLine = true,
        isError = showError,
        supportingText = {
            if (showError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
    )
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(Res.string.password)) },
        singleLine = true,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val (icon, description) = if (passwordVisible) {
                Icons.Outlined.VisibilityOff to stringResource(Res.string.cd_hide_password)
            } else {
                Icons.Outlined.Visibility to stringResource(Res.string.cd_show_password)
            }
            IconButton(onClick = onTogglePasswordVisibility) {
                Icon(imageVector = icon, contentDescription = description)
            }
        },
    )
    Spacer(modifier = Modifier.height(tokens.spacing.xl))
    Button(
        onClick = onSubmit,
        enabled = email.isNotBlank() && password.isNotBlank() && !loading,
        modifier = Modifier
            .fillMaxWidth()
            .height(tokens.spacing.xxl + tokens.spacing.xl),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        if (loading) {
            CircularProgressIndicator(
                strokeWidth = tokens.spacing.xxs,
                modifier = Modifier.size(tokens.spacing.md + tokens.spacing.sm),
                color = MaterialTheme.colorScheme.onPrimary,
            )
        } else {
            Text(text = stringResource(Res.string.login), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview
@DevicePreviews
@Composable
private fun LoginCredentialsSectionPreview() {
    DevicePreviewContainer {
        val tokens = designTokens()
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var visible by remember { mutableStateOf(false) }
        val errorText = stringResource(Res.string.login_error_invalid_credentials)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = tokens.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        ) {
            LoginCredentialsSection(
                email = email,
                onEmailChange = { email = it },
                password = password,
                onPasswordChange = { password = it },
                passwordVisible = visible,
                onTogglePasswordVisibility = { visible = !visible },
                loading = false,
                onSubmit = {},
                showError = true,
                errorMessage = errorText,
            )
        }
    }
}
