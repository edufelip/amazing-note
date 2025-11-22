package com.edufelip.shared.security

expect class SecureKeyStore() {
    fun getOrCreateKey(): ByteArray
}
