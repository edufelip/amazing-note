package com.edufelip.shared.security

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.usePinned
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault

@OptIn(ExperimentalForeignApi::class)
actual fun secureRandomBytes(length: Int): ByteArray {
    val buffer = ByteArray(length)
    val status = buffer.usePinned { pinned ->
        SecRandomCopyBytes(kSecRandomDefault, length.convert(), pinned.addressOf(0))
    }
    if (status != errSecSuccess) error("Unable to generate random bytes: $status")
    return buffer
}
