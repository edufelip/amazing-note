package com.edufelip.shared.security

import java.security.SecureRandom

private val secureRandom = SecureRandom()

actual fun secureRandomBytes(length: Int): ByteArray = ByteArray(length).also(secureRandom::nextBytes)
