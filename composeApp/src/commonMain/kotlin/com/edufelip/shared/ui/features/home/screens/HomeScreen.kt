@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_BETA")

package com.edufelip.shared.ui.features.home.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.edufelip.shared.data.sync.LocalNotesSyncManager
import com.edufelip.shared.data.sync.SyncEvent
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.ui.app.chrome.AmazingTopBar
import com.edufelip.shared.ui.components.organisms.common.NotesEmptyState
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.features.notes.components.ListScreen
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import com.edufelip.shared.ui.util.platform.platformChromeStrategy
import com.edufelip.shared.ui.vm.AuthViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    notes: List<Note>,
    auth: AuthViewModel?,
    onOpenNote: (Note) -> Unit,
    onAdd: () -> Unit,
    onDelete: (Note) -> Unit,
) {
    val currentUserState = auth?.uiState?.collectAsState()?.value
    val currentUid = currentUserState?.user?.uid
    val syncManager = LocalNotesSyncManager.current
    var syncing by remember(currentUid, syncManager) { mutableStateOf(currentUid != null) }

    LaunchedEffect(currentUid, syncManager) {
        syncing = currentUid != null
        if (currentUid != null && syncManager != null) {
            syncManager.events.collect { event ->
                if (event is SyncEvent.SyncCompleted) {
                    syncing = false
                }
            }
        } else {
            syncing = false
        }
    }

    val query = remember { mutableStateOf("") }
    val snackBarHostState = remember { SnackbarHostState() }

    val filteredNotes =
        if (query.value.isBlank()) {
            notes
        } else {
            notes.filter {
                it.title.contains(query.value, ignoreCase = true) ||
                    it.description.contains(query.value, ignoreCase = true)
            }
        }

    val hasNotes = notes.isNotEmpty()
    val chrome = platformChromeStrategy()
    val tokens = designTokens()

    Scaffold(
        topBar = { AmazingTopBar(user = currentUserState?.user) },
        containerColor = Color.Transparent,
        contentWindowInsets = chrome.contentWindowInsets,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            ListScreen(
                notes = filteredNotes,
                onNoteClick = onOpenNote,
                onAddClick = onAdd,
                searchQuery = query.value,
                onSearchQueryChange = { query.value = it },
                onDelete = onDelete,
                snackBarHostState = snackBarHostState,
                showTopAppBar = false,
                hasAnyNotes = hasNotes,
                headerContent = null,
                emptyContent = {
                    NotesEmptyState(
                        onCreateNote = onAdd,
                    )
                },
            )

            if (syncing) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = tokens.colors.accent,
                    trackColor = tokens.colors.accentMuted.copy(alpha = 0.35f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Home")
@DevicePreviews
@Composable
internal fun HomeScreenPreview(
    @PreviewParameter(HomeScreenPreviewProvider::class) state: HomePreviewState,
) {
    DevicePreviewContainer(
        isDarkTheme = state.isDarkTheme,
        localized = state.localized,
    ) {
        HomeScreen(
            notes = state.notes,
            auth = null,
            onOpenNote = {},
            onAdd = {},
            onDelete = {},
        )
    }
}

internal data class HomePreviewState(
    val notes: List<Note>,
    val isDarkTheme: Boolean = false,
    val localized: Boolean = false,
)

internal object HomePreviewSamples {
    private val sampleNotes = List(4) { index ->
        Note(
            id = index + 1,
            title = "Pinned idea #${index + 1}",
            description = "Sample note body to showcase how the list renders in previews.",
            deleted = false,
            createdAt = 1_700_000_000_000L + index * 3_600_000L,
            updatedAt = 1_700_000_000_000L + index * 5_400_000L,
            folderId = if (index % 2 == 0) 1L else 2L,
        )
    }

    val empty = HomePreviewState(notes = emptyList())
    val populated = HomePreviewState(notes = sampleNotes)
    val dark = HomePreviewState(
        notes = sampleNotes,
        isDarkTheme = true,
        localized = true,
    )

    val states: List<HomePreviewState> = listOf(empty, populated, dark)
}

internal expect class HomeScreenPreviewProvider() : PreviewParameterProvider<HomePreviewState> {
    override val values: Sequence<HomePreviewState>
}
