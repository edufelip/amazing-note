package com.edufelip.shared.ui.features.trash.routes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.ui.features.trash.screens.TrashScreen
import com.edufelip.shared.ui.vm.NoteUiViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun TrashRoute(
    viewModel: NoteUiViewModel,
    syncManager: NotesSyncManager,
    coroutineScope: CoroutineScope,
    onBack: () -> Unit,
) {
    val trash by viewModel.trash.collectAsState(initial = emptyList())

    TrashScreen(
        notes = trash,
        onRestore = { note ->
            coroutineScope.launch {
                viewModel.setDeleted(note.id, false)
                syncManager.syncLocalToRemoteOnly()
            }
        },
        onBack = onBack,
    )
}
