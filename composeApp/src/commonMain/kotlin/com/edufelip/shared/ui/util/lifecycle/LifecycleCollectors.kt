package com.edufelip.shared.ui.util.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Composable
expect fun <T> Flow<T>.collectWithLifecycle(initial: T): State<T>

@Composable
expect fun <T> StateFlow<T>.collectWithLifecycle(): State<T>
