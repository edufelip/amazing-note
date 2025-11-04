package com.edufelip.shared.ui.features.trash.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_back
import com.edufelip.shared.resources.trash
import com.edufelip.shared.resources.trash_count
import com.edufelip.shared.ui.components.organisms.trash.TrashTimeline
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    notes: List<Note>,
    onRestore: (Note) -> Unit,
    onBack: () -> Unit,
    onEmptyTrash: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    val titleText = if (notes.isEmpty()) {
                        stringResource(Res.string.trash)
                    } else {
                        stringResource(Res.string.trash_count, notes.size)
                    }
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.cd_back),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TrashTimeline(
                notes = notes,
                onRestore = onRestore,
                onEmptyTrash = onEmptyTrash,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Trash")
@DevicePreviews
@Composable
internal fun TrashScreenPreview(
    @PreviewParameter(TrashScreenPreviewProvider::class) state: TrashPreviewState,
) {
    DevicePreviewContainer(
        isDarkTheme = state.isDarkTheme,
        localized = state.localized,
    ) {
        TrashScreen(
            notes = state.notes,
            onRestore = {},
            onBack = {},
            onEmptyTrash = {},
        )
    }
}

internal data class TrashPreviewState(
    val notes: List<Note>,
    val isDarkTheme: Boolean = false,
    val localized: Boolean = false,
)

internal object TrashPreviewSamples {
    private val now = 1_700_100_000_000L

    private val baseNotes: List<Note> = List(6) { index ->
        Note(
            id = index + 100,
            title = "Trash #${index + 1}",
            description = "Deleted note sample for preview.",
            deleted = true,
            createdAt = now + index * 3_600_000L,
            updatedAt = now + index * 5_400_000L,
        )
    }

    val empty = TrashPreviewState(notes = emptyList())
    val populated = TrashPreviewState(notes = baseNotes)
    val dark = TrashPreviewState(
        notes = baseNotes,
        isDarkTheme = true,
        localized = true,
    )

    val states: List<TrashPreviewState> = listOf(populated, empty, dark)
}

internal expect class TrashScreenPreviewProvider() : PreviewParameterProvider<TrashPreviewState> {
    override val values: Sequence<TrashPreviewState>
}
