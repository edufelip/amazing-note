@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_BETA")

package com.edufelip.shared.ui.features.notes.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.edufelip.shared.domain.model.Folder
import com.edufelip.shared.domain.model.Note
import com.edufelip.shared.resources.Res
import com.edufelip.shared.resources.delete_folder_message
import com.edufelip.shared.resources.folders_empty_hint
import com.edufelip.shared.resources.folders_empty_title
import com.edufelip.shared.resources.folders_empty_unlock_label
import com.edufelip.shared.resources.home_new_folder
import com.edufelip.shared.resources.new_folder
import com.edufelip.shared.resources.rename_folder
import com.edufelip.shared.resources.search_no_results
import com.edufelip.shared.resources.search_reset
import com.edufelip.shared.ui.app.chrome.AmazingTopBar
import com.edufelip.shared.ui.app.chrome.AppChromeDefaults
import com.edufelip.shared.ui.components.organisms.notes.FolderLayout
import com.edufelip.shared.ui.components.organisms.notes.FoldersGrid
import com.edufelip.shared.ui.components.organisms.notes.FoldersHeader
import com.edufelip.shared.ui.components.organisms.notes.FoldersList
import com.edufelip.shared.ui.designsystem.designTokens
import com.edufelip.shared.ui.features.notes.dialogs.DeleteFolderDialog
import com.edufelip.shared.ui.features.notes.dialogs.FolderNameDialog
import com.edufelip.shared.ui.preview.DevicePreviewContainer
import com.edufelip.shared.ui.preview.DevicePreviews
import com.edufelip.shared.ui.util.lifecycle.collectWithLifecycle
import com.edufelip.shared.ui.util.platform.platformChromeStrategy
import com.edufelip.shared.ui.vm.AuthViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(
    folders: List<Folder>,
    notes: List<Note>,
    isDarkTheme: Boolean,
    auth: AuthViewModel? = null,
    layoutMode: FolderLayout,
    onLayoutChange: (FolderLayout) -> Unit,
    onOpenFolder: (Folder) -> Unit,
    onCreateFolder: (String) -> Unit,
    onRenameFolder: (Folder, String) -> Unit,
    onDeleteFolder: (Folder) -> Unit,
    modifier: Modifier = Modifier,
) {
    val notesByFolder =
        remember(notes) { notes.groupBy { it.folderId }.mapValues { it.value.size } }
    val hasFolders = folders.isNotEmpty()

    var searchQuery by rememberSaveable { mutableStateOf("") }

    val filteredFolders = remember(folders, searchQuery) {
        if (searchQuery.isBlank()) {
            folders
        } else {
            folders.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }
    val hasFilteredContent = filteredFolders.isNotEmpty()

    var createDialogVisible by rememberSaveable { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<Folder?>(null) }
    var deleteTarget by remember { mutableStateOf<Folder?>(null) }
    var nameInput by rememberSaveable { mutableStateOf("") }

    fun openCreate() {
        nameInput = ""
        createDialogVisible = true
    }

    fun dismissDialogs() {
        createDialogVisible = false
        renameTarget = null
        deleteTarget = null
        nameInput = ""
    }

    val tokens = designTokens()
    val accentPalette = listOf(
        tokens.colors.accent,
        tokens.colors.accentMuted,
        tokens.colors.info,
        tokens.colors.success,
        tokens.colors.accent.copy(alpha = 0.85f),
    )

    val currentUserState = auth?.uiState?.collectWithLifecycle()?.value

    val isEmpty = folders.isEmpty()
    val chrome = platformChromeStrategy()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { AmazingTopBar(user = currentUserState?.user) },
        containerColor = tokens.colors.canvas,
        contentWindowInsets = chrome.contentWindowInsets,
        floatingActionButton = {
            if (!isEmpty) {
                val navigationBottom = chrome.navigationBarBottomInset()
                val fabBottomPadding = when {
                    chrome.bottomBarHeight == 0.dp -> tokens.spacing.lg
                    else -> chrome.bottomBarHeight + (navigationBottom * 2)
                }
                ExtendedFloatingActionButton(
                    modifier = Modifier.padding(bottom = fabBottomPadding, end = tokens.spacing.sm),
                    onClick = { openCreate() },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = null,
                        )
                    },
                    text = { Text(text = stringResource(Res.string.home_new_folder)) },
                )
            }
        },
    ) { padding ->
        val contentModifier = with(chrome) {
            Modifier
                .fillMaxSize()
                .padding(padding)
                .applyNavigationBarsPadding()
        }

        if (isEmpty) {
            EmptyFoldersState(
                modifier = contentModifier,
                onCreateFolder = { openCreate() },
            )
        } else {
            Column(
                modifier = contentModifier,
                verticalArrangement = Arrangement.Top,
            ) {
                if (hasFolders) {
                    FoldersHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = tokens.spacing.xl),
                        query = searchQuery,
                        onQueryChange = { value -> searchQuery = value },
                        layoutMode = layoutMode,
                        onToggleLayout = {
                            val next = when (layoutMode) {
                                FolderLayout.Grid -> FolderLayout.List
                                FolderLayout.List -> FolderLayout.Grid
                            }
                            onLayoutChange(next)
                        },
                    )
                    Spacer(modifier = Modifier.height(tokens.spacing.sm))
                }

                if (hasFilteredContent) {
                    key(isDarkTheme) {
                        Crossfade(
                            targetState = layoutMode,
                            animationSpec = tween(durationMillis = 220),
                            modifier = Modifier.fillMaxSize(),
                            label = "folders_layout_crossfade",
                        ) { activeLayout ->
                            when (activeLayout) {
                                FolderLayout.Grid -> {
                                    FoldersGrid(
                                        modifier = Modifier.fillMaxSize(),
                                        folders = filteredFolders,
                                        notesByFolder = notesByFolder,
                                        accentPalette = accentPalette,
                                        onOpenFolder = onOpenFolder,
                                        onRequestRename = { folder ->
                                            nameInput = folder.name
                                            renameTarget = folder
                                        },
                                        onRequestDelete = { folder ->
                                            deleteTarget = folder
                                        },
                                    )
                                }

                                FolderLayout.List -> {
                                    FoldersList(
                                        folders = filteredFolders,
                                        notesByFolder = notesByFolder,
                                        accentPalette = accentPalette,
                                        onOpenFolder = onOpenFolder,
                                        onRequestRename = { folder ->
                                            nameInput = folder.name
                                            renameTarget = folder
                                        },
                                        onRequestDelete = { folder ->
                                            deleteTarget = folder
                                        },
                                    )
                                }
                            }
                        }
                    }
                } else {
                    FoldersSearchEmptyState(
                        modifier = with(chrome) {
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = tokens.spacing.xxl)
                                .applyNavigationBarsPadding()
                        },
                        onReset = { searchQuery = "" },
                    )
                }
            }
        }
    }

    if (createDialogVisible) {
        FolderNameDialog(
            title = stringResource(Res.string.new_folder),
            initialValue = nameInput,
            onDismiss = { dismissDialogs() },
            onConfirm = { value ->
                if (value.isNotBlank()) onCreateFolder(value.trim())
                dismissDialogs()
            },
        )
    }

    renameTarget?.let { folder ->
        FolderNameDialog(
            title = stringResource(Res.string.rename_folder),
            initialValue = nameInput.ifEmpty { folder.name },
            onDismiss = { dismissDialogs() },
            onConfirm = { value ->
                if (value.isNotBlank() && value != folder.name) onRenameFolder(folder, value.trim())
                dismissDialogs()
            },
        )
    }

    deleteTarget?.let { folder ->
        DeleteFolderDialog(
            message = stringResource(Res.string.delete_folder_message),
            onDismiss = { dismissDialogs() },
            onConfirm = {
                onDeleteFolder(folder)
                dismissDialogs()
            },
        )
    }
}

@Composable
private fun FoldersSearchEmptyState(
    modifier: Modifier = Modifier,
    onReset: () -> Unit,
) {
    val tokens = designTokens()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.search_no_results),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = tokens.colors.onSurface,
        )
        Spacer(modifier = Modifier.height(tokens.spacing.md))
        Text(
            text = stringResource(Res.string.folders_empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.colors.muted,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(tokens.spacing.xl))
        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(
                containerColor = tokens.colors.accentMuted,
                contentColor = tokens.colors.onSurface,
            ),
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = tokens.colors.accent,
            )
            Spacer(modifier = Modifier.width(tokens.spacing.sm))
            Text(text = stringResource(Res.string.search_reset))
        }
    }
}

@Composable
private fun EmptyFoldersState(
    modifier: Modifier = Modifier,
    onCreateFolder: () -> Unit,
) {
    val chrome = platformChromeStrategy()
    val tokens = designTokens()
    val haloSize = tokens.spacing.xxl * 6
    val cardSize = tokens.spacing.xxl * 5
    val iconSize = tokens.spacing.lg * 3

    Column(
        modifier = with(chrome) {
            modifier
                .padding(horizontal = tokens.spacing.xl)
                .applyNavigationBarsPadding()
                .padding(bottom = AppChromeDefaults.bottomBarHeight)
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        FoldersHeader(
            modifier = Modifier.fillMaxWidth(),
            query = "",
            onQueryChange = {},
            layoutMode = FolderLayout.Grid,
            onToggleLayout = {},
            showControls = false,
        )
        Box(
            modifier = Modifier.size(haloSize),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(haloSize * 0.9f)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                tokens.colors.accent.copy(alpha = 0.25f),
                                Color.Transparent,
                            ),
                        ),
                        shape = CircleShape,
                    ),
            )
            Surface(
                modifier = Modifier.size(cardSize),
                shape = RoundedCornerShape(tokens.radius.lg * 2),
                color = tokens.colors.elevatedSurface.copy(alpha = 0.6f),
                tonalElevation = tokens.elevation.card,
                shadowElevation = tokens.elevation.popover,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(tokens.spacing.md)
                        .background(
                            color = tokens.colors.surface,
                            shape = RoundedCornerShape(tokens.radius.lg + tokens.radius.sm),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FolderOpen,
                            contentDescription = null,
                            tint = tokens.colors.accent.copy(alpha = 0.7f),
                            modifier = Modifier.size(iconSize),
                        )
                        Text(
                            text = stringResource(Res.string.folders_empty_unlock_label),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = tokens.colors.muted,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(tokens.spacing.xxl))
        Text(
            text = stringResource(Res.string.folders_empty_title),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = tokens.colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(tokens.spacing.sm))
        Text(
            text = stringResource(Res.string.folders_empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = tokens.colors.muted,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(tokens.spacing.xxl))
        Button(
            onClick = onCreateFolder,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = tokens.colors.accent,
                contentColor = if (tokens.colors.accent.luminance() > 0.4f) Color.Black else Color.White,
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                horizontal = tokens.spacing.xl,
                vertical = tokens.spacing.md,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.CreateNewFolder,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(tokens.spacing.sm))
            Text(text = stringResource(Res.string.home_new_folder))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Folders")
@DevicePreviews
@Composable
internal fun FoldersScreenPreview(
    @PreviewParameter(FoldersScreenPreviewProvider::class) state: FoldersPreviewState,
) {
    DevicePreviewContainer(
        isDarkTheme = state.isDarkTheme,
        localized = state.localized,
    ) {
        FoldersScreen(
            folders = state.folders,
            notes = state.notes,
            isDarkTheme = state.isDarkTheme,
            layoutMode = FolderLayout.Grid,
            onLayoutChange = {},
            onOpenFolder = {},
            onCreateFolder = {},
            onRenameFolder = { _, _ -> },
            onDeleteFolder = {},
        )
    }
}

internal data class FoldersPreviewState(
    val folders: List<Folder>,
    val notes: List<Note>,
    val isDarkTheme: Boolean = false,
    val localized: Boolean = false,
)

internal object FoldersPreviewSamples {
    private val sampleFolders = listOf(
        Folder(id = 1, name = "Work", createdAt = 1_699_000_000_000, updatedAt = 1_699_100_000_000),
        Folder(
            id = 2,
            name = "Personal",
            createdAt = 1_699_050_000_000,
            updatedAt = 1_699_150_000_000,
        ),
        Folder(
            id = 3,
            name = "Reading List",
            createdAt = 1_699_060_000_000,
            updatedAt = 1_699_170_000_000,
        ),
    )

    private val sampleNotes = buildList {
        addAll(
            listOf(
                Note(
                    id = 11,
                    title = "Project roadmap",
                    description = "Outline the next milestones before Friday.",
                    deleted = false,
                    createdAt = 1_700_000_000_000,
                    updatedAt = 1_700_010_000_000,
                    folderId = 1,
                ),
                Note(
                    id = 12,
                    title = "Design review notes",
                    description = "Summarize feedback from the last meeting.",
                    deleted = false,
                    createdAt = 1_700_020_000_000,
                    updatedAt = 1_700_030_000_000,
                    folderId = 1,
                ),
                Note(
                    id = 21,
                    title = "Groceries",
                    description = "Vegetables, snacks, and coffee beans.",
                    deleted = false,
                    createdAt = 1_700_040_000_000,
                    updatedAt = 1_700_050_000_000,
                    folderId = 2,
                ),
            ),
        )
        add(
            Note(
                id = 31,
                title = "Unread article",
                description = "Revisit the Compose performance guide.",
                deleted = false,
                createdAt = 1_700_060_000_000,
                updatedAt = 1_700_070_000_000,
                folderId = null,
            ),
        )
    }

    val empty = FoldersPreviewState(
        folders = emptyList(),
        notes = emptyList(),
    )

    val populated = FoldersPreviewState(
        folders = sampleFolders,
        notes = sampleNotes,
    )

    val darkLocalized = FoldersPreviewState(
        folders = sampleFolders,
        notes = sampleNotes,
        isDarkTheme = true,
        localized = true,
    )

    val states: List<FoldersPreviewState> = listOf(empty, populated, darkLocalized)
}

internal expect class FoldersScreenPreviewProvider() : PreviewParameterProvider<FoldersPreviewState> {
    override val values: Sequence<FoldersPreviewState>
}
