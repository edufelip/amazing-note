package com.edufelip.shared.ui.nav.screens

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.edufelip.shared.ui.util.platform.Haptics
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveAlertDialog
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import io.github.alexzhirkevich.cupertino.cancel
import io.github.alexzhirkevich.cupertino.default
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun FolderNameDialog(
    title: String? = null,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by rememberSaveable { mutableStateOf(initialValue) }
    val resolvedTitle = title ?: stringResource(Res.string.rename_folder)
    AdaptiveAlertDialog(
        onDismissRequest = {
            Haptics.lightTap()
            onDismiss()
        },
        title = { Text(text = resolvedTitle) },
        message = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                label = { Text(text = stringResource(Res.string.folder_name)) },
            )
        },
    ) {
        cancel(onClick = {
            Haptics.lightTap()
            onDismiss()
        }) {
            Text(text = stringResource(Res.string.dialog_cancel))
        }
        default(onClick = {
            Haptics.lightTap()
            onConfirm(value)
        }) {
            Text(text = stringResource(Res.string.dialog_ok))
        }
    }
}

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun DeleteFolderDialog(
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    title: String? = null,
) {
    val resolvedTitle = title ?: stringResource(Res.string.delete_folder)
    AdaptiveAlertDialog(
        onDismissRequest = {
            Haptics.lightTap()
            onDismiss()
        },
        title = { Text(text = resolvedTitle) },
        message = { Text(text = message) },
    ) {
        cancel(onClick = {
            Haptics.lightTap()
            onDismiss()
        }) {
            Text(text = stringResource(Res.string.dialog_cancel))
        }
        default(onClick = {
            Haptics.lightTap()
            onConfirm()
        }) {
            Text(text = stringResource(Res.string.dialog_ok))
        }
    }
}
