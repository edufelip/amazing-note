package com.edufelip.shared.ui.util.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Composable
actual fun <T> Flow<T>.collectWithLifecycle(initial: T): State<T> = collectAsState(initial = initial)

@Composable
actual fun <T> StateFlow<T>.collectWithLifecycle(): State<T> = collectAsState()
