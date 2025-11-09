package com.edufelip.shared.ui.features.auth.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.ic_google
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun GoogleButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF3C4043),
            disabledContainerColor = Color.White.copy(alpha = 0.6f),
            disabledContentColor = Color(0xFF3C4043).copy(alpha = 0.6f),
        ),
        contentPadding = PaddingValues(horizontal = 16.dp),
        enabled = enabled,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_google),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 12.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFF3C4043),
            )
        }
    }
}

@Preview
@Composable
fun GoogleButtonPreview() {
    GoogleButton(text = "", enabled = true, onClick = {})
}
