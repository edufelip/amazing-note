package com.edufelip.shared.ui.vm

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

open class SharedViewModel(
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
) {
    private val job = SupervisorJob()
    protected val viewModelScope: CoroutineScope = CoroutineScope(job + dispatcher)

    protected fun launchInScope(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch(block = block)
    }

    protected fun <T> Flow<T>.collectInScope() = launchIn(viewModelScope)

    open fun clear() {
        job.cancel()
    }
}
