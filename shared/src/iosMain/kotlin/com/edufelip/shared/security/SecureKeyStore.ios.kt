package com.edufelip.shared.security

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.interpretObjCPointerOrNull
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUserDefaults
import platform.Foundation.create
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecDuplicateItem
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.posix.memcpy

private const val KEY_ALIAS = "note_cipher_key"
private const val SERVICE = "com.edufelip.amazing_note.cipher"
private const val KEY_SIZE_BYTES = 32

actual class SecureKeyStore actual constructor() {

    @OptIn(ExperimentalForeignApi::class)
    actual fun getOrCreateKey(): ByteArray {
        val defaults = NSUserDefaults.standardUserDefaults
        val legacy = defaults.stringForKey(KEY_ALIAS)?.hexToBytes()
        if (legacy != null && legacy.size == KEY_SIZE_BYTES) {
            storeKey(legacy)
            defaults.removeObjectForKey(KEY_ALIAS)
            return legacy
        } else if (legacy != null) {
            defaults.removeObjectForKey(KEY_ALIAS)
        }
        memScoped {
            val query = baseQuery(returnData = true)
            val result = alloc<COpaquePointerVar>()
            val status = SecItemCopyMatching(query, result.ptr)
            when (status) {
                errSecSuccess -> {
                    val data = result.value?.let { interpretObjCPointerOrNull<NSData>(it.rawValue) }
                        ?: error("Keychain returned unexpected type")
                    return data.toByteArray()
                }

                errSecItemNotFound -> {
                    val generated = secureRandomBytes(KEY_SIZE_BYTES)
                    storeKey(generated)
                    return generated
                }

                else -> error("Keychain read failed: $status")
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun storeKey(key: ByteArray) {
        val data = key.toNSData()
        memScoped {
            val addQuery = baseQuery(value = data)
            val status = SecItemAdd(addQuery, null)
            when (status) {
                errSecSuccess -> return
                errSecDuplicateItem -> {
                    // Replace corrupt or invalid key if present.
                    SecItemDelete(baseQuery())
                    val retryStatus = SecItemAdd(addQuery, null)
                    if (retryStatus != errSecSuccess) {
                        error("Keychain replace failed: $retryStatus")
                    }
                }

                else -> error("Keychain write failed: $status")
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun baseQuery(
        returnData: Boolean = false,
        value: NSData? = null,
    ): CFDictionaryRef? = memScoped {
        val dict: CFMutableDictionaryRef? = CFDictionaryCreateMutable(
            null,
            0,
            kCFTypeDictionaryKeyCallBacks.ptr,
            kCFTypeDictionaryValueCallBacks.ptr,
        )

        val service = CFStringCreateWithCString(null, SERVICE, kCFStringEncodingUTF8)
        val account = CFStringCreateWithCString(null, KEY_ALIAS, kCFStringEncodingUTF8)

        CFDictionarySetValue(dict, kSecClass, kSecClassGenericPassword)
        CFDictionarySetValue(dict, kSecAttrService, service)
        CFDictionarySetValue(dict, kSecAttrAccount, account)
        CFDictionarySetValue(dict, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)

        if (returnData) {
            CFDictionarySetValue(dict, kSecReturnData, kCFBooleanTrue)
            CFDictionarySetValue(dict, kSecMatchLimit, kSecMatchLimitOne)
        }
        if (value != null) {
            // Convert NSData to CFDataRef for the keychain API.
            val cfData = value.toCFData()
            CFDictionarySetValue(dict, kSecValueData, cfData)
        }

        dict
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun NSData.toCFData() = CFDataCreate(
    null,
    this.bytes?.reinterpret(),
    this.length.convert(),
)

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = size.convert())
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun NSData.toByteArray(): ByteArray {
    val output = ByteArray(length.toInt())
    output.usePinned { pinned ->
        memcpy(pinned.addressOf(0), bytes, length)
    }
    return output
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun String.toNSString(): NSString = NSString.create(string = this)

private fun String.hexToBytes(): ByteArray? {
    if (length % 2 != 0) return null
    val output = ByteArray(length / 2)
    for (index in output.indices) {
        val hi = charAtHex(index * 2) ?: return null
        val lo = charAtHex(index * 2 + 1) ?: return null
        output[index] = ((hi shl 4) or lo).toByte()
    }
    return output
}

private fun String.charAtHex(position: Int): Int? {
    val value = this[position]
    return when (value) {
        in '0'..'9' -> value.code - '0'.code
        in 'A'..'F' -> value.code - 'A'.code + 10
        in 'a'..'f' -> value.code - 'a'.code + 10
        else -> null
    }
}
