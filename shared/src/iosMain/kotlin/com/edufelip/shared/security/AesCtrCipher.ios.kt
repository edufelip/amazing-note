@file:OptIn(ExperimentalForeignApi::class)

package com.edufelip.shared.security

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.ULongVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreCrypto.CCCryptorCreateWithMode
import platform.CoreCrypto.CCCryptorFinal
import platform.CoreCrypto.CCCryptorRefVar
import platform.CoreCrypto.CCCryptorRelease
import platform.CoreCrypto.CCCryptorUpdate
import platform.CoreCrypto.CCHmac
import platform.CoreCrypto.ccNoPadding
import platform.CoreCrypto.kCCAlgorithmAES
import platform.CoreCrypto.kCCDecrypt
import platform.CoreCrypto.kCCEncrypt
import platform.CoreCrypto.kCCHmacAlgSHA256
import platform.CoreCrypto.kCCModeCTR
import platform.CoreCrypto.kCCSuccess

private const val SHA256_LENGTH = 32

internal actual fun aesCtrEncrypt(key: ByteArray, iv: ByteArray, plaintext: ByteArray): ByteArray = processAesCtr(encrypt = true, key = key, iv = iv, input = plaintext)

internal actual fun aesCtrDecrypt(key: ByteArray, iv: ByteArray, ciphertext: ByteArray): ByteArray = processAesCtr(encrypt = false, key = key, iv = iv, input = ciphertext)

internal actual fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
    val output = ByteArray(SHA256_LENGTH)
    key.usePinned { keyPinned ->
        data.usePinned { dataPinned ->
            output.usePinned { outPinned ->
                CCHmac(
                    kCCHmacAlgSHA256,
                    keyPinned.addressOf(0).reinterpret<UByteVar>(),
                    key.size.convert<ULong>(),
                    dataPinned.addressOf(0).reinterpret<UByteVar>(),
                    data.size.convert<ULong>(),
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
    val status = memScoped {
        val cryptorRef = alloc<CCCryptorRefVar>()
        val createStatus =
            key.usePinned { keyPinned ->
                iv.usePinned { ivPinned ->
                    CCCryptorCreateWithMode(
                        if (encrypt) kCCEncrypt else kCCDecrypt,
                        kCCModeCTR,
                        kCCAlgorithmAES,
                        ccNoPadding,
                        ivPinned.addressOf(0).reinterpret<UByteVar>(),
                        keyPinned.addressOf(0).reinterpret<UByteVar>(),
                        key.size.convert<ULong>(),
                        null,
                        0uL,
                        0,
                        0u,
                        cryptorRef.ptr,
                    )
                }
            }
        if (createStatus != kCCSuccess) {
            return@memScoped createStatus
        }
        val cryptor = cryptorRef.value ?: return@memScoped createStatus
        val bytesProcessed = alloc<ULongVar>()
        val updateStatus =
            input.usePinned { inputPinned ->
                output.usePinned { outPinned ->
                    CCCryptorUpdate(
                        cryptor,
                        inputPinned.addressOf(0).reinterpret<UByteVar>(),
                        input.size.convert<ULong>(),
                        outPinned.addressOf(0).reinterpret<UByteVar>(),
                        output.size.convert<ULong>(),
                        bytesProcessed.ptr,
                    )
                }
            }
        val finalStatus =
            if (updateStatus == kCCSuccess) {
                CCCryptorFinal(
                    cryptor,
                    null,
                    0uL,
                    bytesProcessed.ptr,
                )
            } else {
                updateStatus
            }
        CCCryptorRelease(cryptor)
        finalStatus
    }
    check(status == kCCSuccess) { "AES CTR failure with status $status" }
    return output
}
