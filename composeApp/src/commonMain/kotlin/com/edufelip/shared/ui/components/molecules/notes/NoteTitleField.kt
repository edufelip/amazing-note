package com.edufelip.shared.ui.components.molecules.notes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.title
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteTitleField(
    titleState: TextFieldValue,
    onTitleChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    textStyle: TextStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
) {
    Column(modifier = modifier) {
        TextField(
            value = titleState,
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = textStyle,
            placeholder = { Text(text = stringResource(Res.string.title)) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
            isError = error != null,
        )
        if (!error.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Preview
@Composable
private fun NoteTitleFieldPreview() {
    NoteTitleField(
        titleState = TextFieldValue("Meeting notes"),
        onTitleChange = {},
        error = null,
    )
}
