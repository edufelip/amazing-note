package com.edufelip.shared.ui.components.atoms.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews

@Composable
fun CircularIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    enabled: Boolean = true,
    background: Color? = null,
    tint: Color? = null,
    modifier: Modifier = Modifier,
) {
    val tokens = designTokens()
    val buttonSize = tokens.spacing.xxl + tokens.spacing.md
    val appliedBackground = background ?: tokens.colors.elevatedSurface
    val appliedTint = tint ?: if (appliedBackground.luminance() < 0.5f) Color.White else Color.Black
    Surface(
        modifier = modifier
            .size(buttonSize)
            .background(Color.Transparent, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        shape = CircleShape,
        color = appliedBackground,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = contentDescription, tint = appliedTint)
        }
    }
}

@DevicePreviews
@Composable
private fun CircularIconButtonPreview() {
    DevicePreviewContainer {
        CircularIconButton(
            icon = Icons.Filled.Check,
            contentDescription = null,
            onClick = {},
        )
    }
}
