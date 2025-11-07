package com.edufelip.shared.ui.components.organisms.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.editor_add_image
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteEditorActionBar(
    onAddImage: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    if (onAddImage == null) {
        return
    }
    val tokens = designTokens()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = tokens.spacing.lg,
                bottom = tokens.spacing.lg + 8.dp,
            ),
        horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val accent = tokens.colors.accent
        val contentColor = if (accent.luminance() > 0.4f) Color.Black else Color.White
        Button(
            onClick = onAddImage,
            colors = ButtonDefaults.buttonColors(
                containerColor = accent,
                contentColor = contentColor,
            ),
            shape = RoundedCornerShape(tokens.radius.sm),
        ) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = stringResource(Res.string.editor_add_image),
                tint = contentColor,
            )
            Spacer(modifier = Modifier.size(tokens.spacing.sm))
            Text(text = stringResource(Res.string.editor_add_image), color = contentColor)
        }
    }
}

@DevicePreviews
@Composable
private fun NoteEditorActionBarPreview() {
    DevicePreviewContainer {
        NoteEditorActionBar(onAddImage = {})
    }
}
