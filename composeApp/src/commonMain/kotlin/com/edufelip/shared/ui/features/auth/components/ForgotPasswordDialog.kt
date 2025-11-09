package com.edufelip.shared.ui.features.auth.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.dialog_cancel
import com.edufelip.shared.resources.email
import com.edufelip.shared.resources.forgot_password
import com.edufelip.shared.resources.forgot_password_description
import com.edufelip.shared.resources.forgot_password_send_email
import com.edufelip.shared.ui.preview.DevicePreviews
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ForgotPasswordDialog(
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
                    text = stringResource(Res.string.forgot_password_description),
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
                    Text(text = stringResource(Res.string.forgot_password_send_email))
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !loading,
            ) {
                Text(text = stringResource(Res.string.dialog_cancel))
            }
        },
    )
}

@Composable
@Preview
@DevicePreviews
private fun ForgotPasswordDialogPreview() {
    ForgotPasswordDialog(
        email = "",
        onEmailChange = {},
        onDismiss = {},
        onSubmit = {},
        loading = false,
    )
}
