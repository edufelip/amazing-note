package com.edufelip.shared.ui.features.notes.dialogs

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.dialog_cancel
import com.edufelip.shared.resources.note_discard_confirm
import com.edufelip.shared.resources.note_discard_message
import com.edufelip.shared.resources.note_discard_title
import com.edufelip.shared.resources.note_delete_confirm
import com.edufelip.shared.resources.note_delete_message
import com.edufelip.shared.resources.note_delete_title
import com.edufelip.shared.ui.util.platform.Haptics
import com.slapps.cupertino.adaptive.AdaptiveAlertDialog
import com.slapps.cupertino.adaptive.ExperimentalAdaptiveApi
import com.slapps.cupertino.cancel
import com.slapps.cupertino.default
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun DiscardNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AdaptiveAlertDialog(
        onDismissRequest = {
            Haptics.lightTap()
            onDismiss()
        },
        title = {
            Text(
                text = stringResource(Res.string.note_discard_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        message = {
            Text(
                text = stringResource(Res.string.note_discard_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        adaptation = {
            material {
                containerColor = MaterialTheme.colorScheme.surface
            }
            cupertino {
                containerColor = MaterialTheme.colorScheme.surface
            }
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
            onConfirm()
        }) {
            Text(
                text = stringResource(Res.string.note_discard_confirm),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun DeleteNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AdaptiveAlertDialog(
        onDismissRequest = {
            Haptics.lightTap()
            onDismiss()
        },
        title = {
            Text(
                text = stringResource(Res.string.note_delete_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        message = {
            Text(
                text = stringResource(Res.string.note_delete_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        adaptation = {
            material {
                containerColor = MaterialTheme.colorScheme.surface
            }
            cupertino {
                containerColor = MaterialTheme.colorScheme.surface
            }
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
            onConfirm()
        }) {
            Text(
                text = stringResource(Res.string.note_delete_confirm),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
