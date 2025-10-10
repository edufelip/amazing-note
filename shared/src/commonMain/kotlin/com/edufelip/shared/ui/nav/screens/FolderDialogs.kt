package com.edufelip.shared.ui.nav.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.delete_folder
import com.edufelip.shared.resources.dialog_cancel
import com.edufelip.shared.resources.dialog_ok
import com.edufelip.shared.resources.folder_name
import com.edufelip.shared.resources.rename_folder
import org.jetbrains.compose.resources.stringResource

@Composable
fun FolderNameDialog(
    title: String? = null,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by rememberSaveable { mutableStateOf(initialValue) }
    val resolvedTitle = title ?: stringResource(Res.string.rename_folder)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = resolvedTitle) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                label = { Text(text = stringResource(Res.string.folder_name)) },
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(value) }) {
                Text(text = stringResource(Res.string.dialog_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.dialog_cancel))
            }
        },
    )
}

@Composable
fun DeleteFolderDialog(
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String? = null,
) {
    val resolvedTitle = title ?: stringResource(Res.string.delete_folder)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = resolvedTitle) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(text = stringResource(Res.string.dialog_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(text = stringResource(Res.string.dialog_cancel)) }
        },
    )
}
