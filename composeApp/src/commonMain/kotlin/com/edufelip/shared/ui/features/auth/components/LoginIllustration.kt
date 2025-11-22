package com.edufelip.shared.ui.features.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.edufelip.shared.ui.preview.DevicePreviews
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun LoginIllustration(modifier: Modifier = Modifier) {
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
@Preview
@DevicePreviews
private fun LoginIllustrationPreview() {
    LoginIllustration()
}
