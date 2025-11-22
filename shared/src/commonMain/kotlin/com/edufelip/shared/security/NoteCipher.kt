package com.edufelip.shared.security

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private const val PREFIX = "ENC:"
internal const val NONCE_SIZE = 16
internal const val TAG_SIZE = 32
private const val LEGACY_NONCE_SIZE = 16
private val ENC_LABEL = "note/enc".encodeToByteArray()
private val MAC_LABEL = "note/mac".encodeToByteArray()

object NoteCipher {
    private val keyStore = SecureKeyStore()
    private val realKey: ByteArray by lazy { ensureKeyLength(keyStore.getOrCreateKey()) }

    private var overrideKey: ByteArray? = null

    @OptIn(ExperimentalEncodingApi::class)
    fun encrypt(value: String): String {
        if (value.isEmpty()) return value
        val plaintext = value.encodeToByteArray()
        val sealed = encryptPayload(activeKey(), plaintext)
        return PREFIX + Base64.Default.encode(sealed)
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun decrypt(value: String): String {
        if (value.isEmpty() || !value.startsWith(PREFIX)) return value
        val payload = runCatching { Base64.Default.decode(value.removePrefix(PREFIX)) }
            .getOrElse { return value }
        val plaintext = runCatching { decryptPayload(activeKey(), payload) }
            .getOrElse { legacyDecrypt(payload) ?: return value }
        return plaintext.decodeToString()
    }

    internal fun overrideKeyForTests(testKey: ByteArray) {
        overrideKey = ensureKeyLength(testKey.copyOf())
    }

    internal fun clearKeyOverride() {
        overrideKey = null
    }

    private fun activeKey(): ByteArray = overrideKey ?: realKey

    private fun ensureKeyLength(bytes: ByteArray): ByteArray {
        require(bytes.size == 32) { "Encryption key must be 256 bits but was ${bytes.size * 8} bits" }
        return bytes
    }

    private fun legacyDecrypt(payload: ByteArray): ByteArray? {
        if (payload.size <= LEGACY_NONCE_SIZE) return null
        val nonce = payload.copyOfRange(0, LEGACY_NONCE_SIZE)
        val body = payload.copyOfRange(LEGACY_NONCE_SIZE, payload.size)
        val keystream = legacyKeystream(activeKey(), nonce, body.size)
        val bytes = ByteArray(body.size) { index ->
            (body[index].toInt() xor keystream[index].toInt()).toByte()
        }
        return bytes
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

    private fun encryptPayload(key: ByteArray, plaintext: ByteArray): ByteArray {
        val encKey = deriveEncryptionKey(key)
        val macKey = deriveMacKey(key)
        val nonce = secureRandomBytes(NONCE_SIZE)
        val cipher = aesCtrEncrypt(encKey, nonce, plaintext)
        val macInput = nonce + cipher
        val tag = hmacSha256(macKey, macInput)
        return macInput + tag
    }

    private fun decryptPayload(key: ByteArray, payload: ByteArray): ByteArray {
        if (payload.size <= NONCE_SIZE + TAG_SIZE) error("Invalid payload")
        val encKey = deriveEncryptionKey(key)
        val macKey = deriveMacKey(key)
        val nonce = payload.copyOfRange(0, NONCE_SIZE)
        val cipher = payload.copyOfRange(NONCE_SIZE, payload.size - TAG_SIZE)
        val tag = payload.copyOfRange(payload.size - TAG_SIZE, payload.size)
        val expected = hmacSha256(macKey, nonce + cipher)
        if (!constantTimeEquals(expected, tag)) error("Authentication failed")
        return aesCtrDecrypt(encKey, nonce, cipher)
    }

    private fun deriveEncryptionKey(key: ByteArray): ByteArray = hmacSha256(key, ENC_LABEL)

    private fun deriveMacKey(key: ByteArray): ByteArray = hmacSha256(key, MAC_LABEL)

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var diff = 0
        for (index in a.indices) {
            diff = diff or (a[index].toInt() xor b[index].toInt())
        }
        return diff == 0
    }
}

expect fun secureRandomBytes(length: Int): ByteArray

internal expect fun aesCtrEncrypt(key: ByteArray, iv: ByteArray, plaintext: ByteArray): ByteArray

internal expect fun aesCtrDecrypt(key: ByteArray, iv: ByteArray, ciphertext: ByteArray): ByteArray

internal expect fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray
