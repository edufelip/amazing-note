@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_BETA")

package com.edufelip.shared.ui.features.home.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.edufelip.shared.data.sync.LocalNotesSyncManager
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.cd_clear_search
import com.edufelip.shared.resources.cd_search
import com.edufelip.shared.resources.logout
import com.edufelip.shared.ui.app.chrome.AmazingTopBar
import com.edufelip.shared.ui.components.atoms.common.AvatarImage
import com.edufelip.shared.ui.components.organisms.common.NotesEmptyState
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.features.notes.components.ListScreen
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import com.edufelip.shared.ui.util.lifecycle.collectWithLifecycle
import com.edufelip.shared.ui.vm.AuthViewModel
import org.jetbrains.compose.resources.stringResource
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
    onAvatarClick: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    val currentUserState = auth?.uiState?.collectWithLifecycle()?.value
    val currentUid = currentUserState?.user?.uid
    val syncManager = LocalNotesSyncManager.current
    val syncingState = syncManager?.syncing?.collectAsState()
    val syncing by remember(currentUid, syncingState?.value) {
        derivedStateOf { currentUid != null && (syncingState?.value == true) }
    }

    val query = remember { mutableStateOf("") }
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    var showAccountSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
    val tokens = designTokens()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    LaunchedEffect(hasNotes) {
        if (!hasNotes) {
            searchVisible = false
            query.value = ""
        }
    }

    Scaffold(
        topBar = {
            AmazingTopBar(
                user = currentUserState?.user,
                onAvatarClick = {
                    if (currentUserState?.user == null) {
                        onAvatarClick()
                    } else {
                        showAccountSheet = true
                    }
                },
                actions = {
                    if (hasNotes) {
                        IconButton(
                            onClick = {
                                if (searchVisible) {
                                    searchVisible = false
                                    query.value = ""
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                } else {
                                    searchVisible = true
                                }
                            },
                        ) {
                            val isOpen = searchVisible
                            Icon(
                                imageVector = if (isOpen) Icons.Filled.Close else Icons.Filled.Search,
                                contentDescription = if (isOpen) {
                                    stringResource(Res.string.cd_clear_search)
                                } else {
                                    stringResource(Res.string.cd_search)
                                },
                                tint = tokens.colors.onSurface,
                            )
                        }
                    }
                },
            )
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(),
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
                showTopAppBar = false,
                searchVisible = searchVisible,
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

    if (showAccountSheet && currentUserState?.user != null) {
        ModalBottomSheet(
            onDismissRequest = { showAccountSheet = false },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(tokens.spacing.xl),
                verticalArrangement = Arrangement.spacedBy(tokens.spacing.md),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(tokens.spacing.md),
                ) {
                    AvatarImage(
                        photoUrl = currentUserState.user.photoUrl,
                        size = tokens.spacing.xxl,
                    )
                    Column {
                        Text(
                            text = currentUserState.user.displayName?.takeIf { it.isNotBlank() }
                                ?: currentUserState.user.email.orEmpty(),
                            style = MaterialTheme.typography.titleMedium,
                            color = tokens.colors.onSurface,
                        )
                        currentUserState.user.email?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = tokens.colors.muted,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(tokens.spacing.xxl))

                OutlinedButton(
                    onClick = {
                        showAccountSheet = false
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = tokens.spacing.md),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        modifier = Modifier.padding(end = tokens.spacing.sm),
                    )
                    Text(text = stringResource(Res.string.logout))
                }
                Spacer(modifier = Modifier.height(tokens.spacing.md + 8.dp))
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
            onLogout = {},
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
