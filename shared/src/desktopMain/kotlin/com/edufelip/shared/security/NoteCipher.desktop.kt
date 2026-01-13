package com.edufelip.shared.security

actual fun secureRandomBytes(length: Int): ByteArray = ByteArray(length) { it.toByte() }

internal actual fun aesCtrEncrypt(key: ByteArray, iv: ByteArray, plaintext: ByteArray): ByteArray = ByteArray(plaintext.size) { (plaintext[it].toInt() xor key[it % key.size].toInt() xor iv[it % iv.size].toInt()).toByte() }

internal actual fun aesCtrDecrypt(key: ByteArray, iv: ByteArray, ciphertext: ByteArray): ByteArray = aesCtrEncrypt(key, iv, ciphertext)

internal actual fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
    val result = ByteArray(32)
    for (i in 0 until 32) {
        result[i] = (key[i % key.size].toInt() xor data[i % data.size].toInt()).toByte()
    }
    return result
}
