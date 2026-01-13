@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.edufelip.shared

import kotlinx.cinterop.toKString
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.concurrent.ThreadLocal
import kotlin.native.setUnhandledExceptionHook
import platform.posix.fclose
import platform.posix.fflush
import platform.posix.fopen
import platform.posix.fputs
import platform.posix.getenv
import platform.posix.stderr

@ThreadLocal
private object UnhandledExceptionHookState {
    var installed: Boolean = false
}

private fun appendUnhandledExceptionToTmp(payload: String) {
    val tmpDir = getenv("TMPDIR")?.toKString() ?: "/tmp/"
    val normalizedDir = if (tmpDir.endsWith("/")) tmpDir else "$tmpDir/"
    val path = normalizedDir + "kotlin_unhandled_exception.log"
    val file = fopen(path, "a") ?: return
    fputs(payload, file)
    fputs("\n", file)
    fclose(file)
}

@OptIn(ExperimentalNativeApi::class)
internal fun installUnhandledExceptionHook() {
    if (UnhandledExceptionHookState.installed) return
    UnhandledExceptionHookState.installed = true
    setUnhandledExceptionHook { throwable ->
        val message = throwable.message ?: "no message"
        val stack = runCatching { throwable.stackTraceToString() }.getOrElse { throwable.toString() }
        val payload = "KotlinUnhandledException: ${throwable::class.simpleName}: $message\n$stack"
        appendUnhandledExceptionToTmp(payload)
        fputs(payload, stderr)
        fputs("\n", stderr)
        fflush(stderr)
        println(payload)
    }
}
