package com.edufelip.shared.ui.screens

import androidx.compose.runtime.Composable
import com.edufelip.shared.model.Folder
import com.edufelip.shared.model.Note
import com.edufelip.shared.ui.nav.screens.FoldersScreen
import com.edufelip.shared.ui.preview.PreviewLocalized
import com.edufelip.shared.ui.preview.ScreenPreviewsDarkLight

private fun sampleFolders(): List<Folder> = listOf(
    Folder(id = 1, name = "Work", createdAt = 1_699_000_000_000, updatedAt = 1_699_100_000_000),
    Folder(id = 2, name = "Personal", createdAt = 1_699_050_000_000, updatedAt = 1_699_150_000_000),
    Folder(id = 3, name = "Reading List", createdAt = 1_699_060_000_000, updatedAt = 1_699_170_000_000),
)

private fun sampleNotes(): List<Note> = buildList {
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

@ScreenPreviewsDarkLight
@Composable
fun FoldersScreen_EmptyPreview() {
    PreviewLocalized {
        FoldersScreen(
            folders = emptyList(),
            notes = emptyList(),
            onOpenFolder = {},
            onCreateFolder = {},
            onRenameFolder = { _, _ -> },
            onDeleteFolder = {},
        )
    }
}

@ScreenPreviewsDarkLight
@Composable
fun FoldersScreen_PopulatedPreview() {
    PreviewLocalized {
        FoldersScreen(
            folders = sampleFolders(),
            notes = sampleNotes(),
            onOpenFolder = {},
            onCreateFolder = {},
            onRenameFolder = { _, _ -> },
            onDeleteFolder = {},
        )
    }
}
