package com.edufelip.shared.security

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

private val TEST_KEY = ByteArray(32) { index -> (index * 37 % 256).toByte() }

class NoteCipherTest {

    @AfterTest
    fun tearDown() {
        NoteCipher.clearKeyOverride()
    }

    @Test
    fun encryptThenDecryptRoundTrip() {
        NoteCipher.overrideKeyForTests(TEST_KEY)
        val original = "Secret message"
        val encrypted = NoteCipher.encrypt(original)
        val decrypted = NoteCipher.decrypt(encrypted)
        assertNotEquals(original, encrypted)
        assertEquals(original, decrypted)
    }

    @Test
    fun encryptProducesDifferentCiphertextForSameInput() {
        NoteCipher.overrideKeyForTests(TEST_KEY)
        val original = "repeatable content"
        val first = NoteCipher.encrypt(original)
        val second = NoteCipher.encrypt(original)
        assertTrue(first != second)
    }

    @Test
    fun decryptLegacyPayloadStillWorks() {
        NoteCipher.overrideKeyForTests(TEST_KEY)
        val legacyEncrypted = legacyEncrypt(TEST_KEY, "Legacy cipher text")
        val decrypted = NoteCipher.decrypt(legacyEncrypted)
        assertEquals("Legacy cipher text", decrypted)
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun legacyEncrypt(key: ByteArray, value: String): String {
        val nonce = ByteArray(16) { index -> (index * 11 + 7).toByte() }
        val plainBytes = value.encodeToByteArray()
        val keystream = legacyKeystream(key, nonce, plainBytes.size)
        val cipher = ByteArray(plainBytes.size) { index ->
            (plainBytes[index].toInt() xor keystream[index].toInt()).toByte()
        }
        return "ENC:" + Base64.Default.encode(nonce + cipher)
    }

    private fun legacyKeystream(key: ByteArray, nonce: ByteArray, length: Int): ByteArray {
        var state = 1469598103934665603UL
        (key + nonce).forEach { byte ->
            state = (state xor byte.toUByte().toULong()) * 1099511628211UL
        }
        val stream = ByteArray(length)
        for (index in 0 until length) {
            state = (state * 2862933555777941757UL + 3037000493UL) and ULong.MAX_VALUE
            stream[index] = (state shr 24).toByte()
        }
        return stream
    }
}
