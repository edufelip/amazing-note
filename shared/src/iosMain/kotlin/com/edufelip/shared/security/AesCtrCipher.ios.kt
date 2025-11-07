@file:OptIn(ExperimentalForeignApi::class)

package com.edufelip.shared.security

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import noteCipher.bridge.noteCipherAesCtrCrypt
import noteCipher.bridge.noteCipherHmacSha256

private const val STATUS_SUCCESS = 0
private const val SHA256_LENGTH = 32

internal actual fun aesCtrEncrypt(key: ByteArray, iv: ByteArray, plaintext: ByteArray): ByteArray = processAesCtr(encrypt = true, key = key, iv = iv, input = plaintext)

internal actual fun aesCtrDecrypt(key: ByteArray, iv: ByteArray, ciphertext: ByteArray): ByteArray = processAesCtr(encrypt = false, key = key, iv = iv, input = ciphertext)

internal actual fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
    val output = ByteArray(SHA256_LENGTH)
    key.usePinned { keyPinned ->
        data.usePinned { dataPinned ->
            output.usePinned { outPinned ->
                noteCipherHmacSha256(
                    keyPinned.addressOf(0).reinterpret<UByteVar>(),
                    key.size.convert(),
                    dataPinned.addressOf(0).reinterpret<UByteVar>(),
                    data.size.convert(),
                    outPinned.addressOf(0).reinterpret<UByteVar>(),
                )
            }
        }
    }
    return output
}

private fun processAesCtr(
    encrypt: Boolean,
    key: ByteArray,
    iv: ByteArray,
    input: ByteArray,
): ByteArray {
    require(iv.size == NONCE_SIZE) { "Unexpected IV length: ${iv.size}" }
    val output = ByteArray(input.size)
    val status = key.usePinned { keyPinned ->
        iv.usePinned { ivPinned ->
            input.usePinned { inputPinned ->
                output.usePinned { outPinned ->
                    noteCipherAesCtrCrypt(
                        if (encrypt) 1 else 0,
                        keyPinned.addressOf(0).reinterpret<UByteVar>(),
                        key.size.convert(),
                        ivPinned.addressOf(0).reinterpret<UByteVar>(),
                        iv.size.convert(),
                        inputPinned.addressOf(0).reinterpret<UByteVar>(),
                        input.size.convert(),
                        outPinned.addressOf(0).reinterpret<UByteVar>(),
                    )
                }
            }
        }
    }
    check(status == STATUS_SUCCESS) { "AES CTR failure with status $status" }
    return output
}
