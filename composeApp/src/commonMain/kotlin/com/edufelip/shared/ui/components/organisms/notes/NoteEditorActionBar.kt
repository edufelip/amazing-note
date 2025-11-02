package com.edufelip.shared.ui.components.organisms.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_add
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteEditorActionBar(
    onAddImage: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = { onAddImage?.invoke() },
            enabled = onAddImage != null,
        ) {
            Icon(imageVector = Icons.Outlined.Image, contentDescription = stringResource(Res.string.cd_add))
            Spacer(modifier = Modifier.size(8.dp))
            Text(text = stringResource(Res.string.cd_add))
        }
    }
}

@Preview
@Composable
private fun NoteEditorActionBarPreview() {
    NoteEditorActionBar(onAddImage = {})
}
