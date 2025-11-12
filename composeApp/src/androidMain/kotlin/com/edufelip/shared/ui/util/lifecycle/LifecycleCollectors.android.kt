package com.edufelip.shared.ui.util.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Composable
actual fun <T> Flow<T>.collectWithLifecycle(initial: T): State<T> = collectAsStateWithLifecycle(initialValue = initial)

@Composable
actual fun <T> StateFlow<T>.collectWithLifecycle(): State<T> = collectAsStateWithLifecycle()
