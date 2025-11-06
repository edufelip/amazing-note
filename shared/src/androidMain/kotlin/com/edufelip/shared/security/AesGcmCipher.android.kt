package com.edufelip.shared.security

import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

private const val AES_TRANSFORMATION = "AES/CTR/NoPadding"
private const val AES_ALGORITHM = "AES"
private const val HMAC_ALGORITHM = "HmacSHA256"

internal actual fun aesCtrEncrypt(key: ByteArray, iv: ByteArray, plaintext: ByteArray): ByteArray =
    processCipher(Cipher.ENCRYPT_MODE, key, iv, plaintext)

internal actual fun aesCtrDecrypt(key: ByteArray, iv: ByteArray, ciphertext: ByteArray): ByteArray =
    processCipher(Cipher.DECRYPT_MODE, key, iv, ciphertext)

internal actual fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
    val mac = Mac.getInstance(HMAC_ALGORITHM)
    val secretKey = SecretKeySpec(key, HMAC_ALGORITHM)
    mac.init(secretKey)
    return mac.doFinal(data)
}

private fun processCipher(mode: Int, key: ByteArray, iv: ByteArray, input: ByteArray): ByteArray {
    val cipher = Cipher.getInstance(AES_TRANSFORMATION)
    val secretKey = SecretKeySpec(key, AES_ALGORITHM)
    val ivSpec = IvParameterSpec(iv)
    cipher.init(mode, secretKey, ivSpec)
    return cipher.doFinal(input)
}
