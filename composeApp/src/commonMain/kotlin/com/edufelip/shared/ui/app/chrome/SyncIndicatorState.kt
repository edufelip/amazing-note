package com.edufelip.shared.ui.app.chrome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.edufelip.shared.core.time.nowEpochMs
import com.edufelip.shared.data.network.NetworkStatus
import com.edufelip.shared.data.sync.NotesSyncManager
import com.edufelip.shared.data.sync.SyncEvent
import com.edufelip.shared.ui.util.lifecycle.collectWithLifecycle
import com.edufelip.shared.ui.util.time.formatSyncTimestamp
import kotlinx.coroutines.launch

sealed interface SyncIndicatorState {
    data object Syncing : SyncIndicatorState
    data class Success(val formatted: String) : SyncIndicatorState
    data object Failed : SyncIndicatorState
}

@Composable
fun rememberSyncIndicatorState(
    syncManager: NotesSyncManager?,
    networkStatus: NetworkStatus?,
    isAuthenticated: Boolean,
): SyncIndicatorState? {
    if (syncManager == null || networkStatus == null) return null

    val isOnline by networkStatus.isOnline.collectWithLifecycle()
    val isSyncing by syncManager.syncing.collectWithLifecycle()
    var lastSuccessAt by remember(syncManager) { mutableStateOf<Long?>(null) }
    var lastFailureAt by remember(syncManager) { mutableStateOf<Long?>(null) }

    LaunchedEffect(syncManager) {
        syncManager.events.collect { event ->
            when (event) {
                SyncEvent.SyncStarted -> {
                    lastFailureAt = null
                }
                SyncEvent.SyncCompleted -> {
                    lastSuccessAt = nowEpochMs()
                    lastFailureAt = null
                }
                is SyncEvent.SyncFailed -> {
                    lastFailureAt = nowEpochMs()
                }
                is SyncEvent.OverwritesApplied -> Unit
            }
        }
    }

    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            lastSuccessAt = null
            lastFailureAt = null
        }
    }

    if (!isAuthenticated || !isOnline) return null
    if (isSyncing) return SyncIndicatorState.Syncing
    if (lastFailureAt != null) return SyncIndicatorState.Failed
    val lastSuccess = lastSuccessAt ?: return null
    val formatted = formatSyncTimestamp(lastSuccess, nowEpochMs())
    return SyncIndicatorState.Success(formatted)
}

@Composable
fun rememberSyncRetryAction(syncManager: NotesSyncManager?): () -> Unit {
    val scope = rememberCoroutineScope()
    return remember(syncManager) {
        {
            syncManager?.let { manager ->
                scope.launch { manager.syncNow() }
            }
        }
    }
}
