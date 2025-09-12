package com.edufelip.shared.ui.gadgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.user_placeholder
import org.jetbrains.compose.resources.painterResource

@Composable
actual fun AvatarImage(photoUrl: String?, modifier: Modifier) {
    val shapeMod = modifier.size(48.dp).clip(CircleShape)
    if (!photoUrl.isNullOrBlank()) {
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = shapeMod,
        )
    } else {
        Box(modifier = shapeMod.background(MaterialTheme.colorScheme.surfaceVariant)) {
            Image(
                painter = painterResource(Res.drawable.user_placeholder),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}
