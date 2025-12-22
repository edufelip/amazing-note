package com.edufelip.shared.security

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class NoteCipherTest {

    private val testKey = ByteArray(32) { it.toByte() }

    @BeforeTest
    fun setUp() {
        NoteCipher.overrideKeyForTests(testKey)
    }

    @AfterTest
    fun tearDown() {
        NoteCipher.clearKeyOverride()
    }

    @Test
    fun encryptionAndDecryptionReturnsOriginalString() {
        val original = "Hello Secure World!"
        val encrypted = NoteCipher.encrypt(original)
        val decrypted = NoteCipher.decrypt(encrypted)

        assertNotEquals(original, encrypted)
        assertEquals(original, decrypted)
    }

    @Test
    fun emptyStringReturnsAsIs() {
        assertEquals("", NoteCipher.encrypt(""))
        assertEquals("", NoteCipher.decrypt(""))
    }

    @Test
    fun nonEncryptedPrefixReturnsAsIs() {
        val plain = "Plain text"
        assertEquals(plain, NoteCipher.decrypt(plain))
    }

    @Test
    fun authenticationFailureReturnsOriginalEncryptedString() {
        val original = "Sensitive data"
        val encrypted = NoteCipher.encrypt(original)

        // Corrupt the tag (last 32 bytes)
        val corrupted = encrypted.substring(0, encrypted.length - 1) + if (encrypted.last() == 'A') 'B' else 'A'

        val decrypted = NoteCipher.decrypt(corrupted)
        assertEquals(corrupted, decrypted)
    }
}
