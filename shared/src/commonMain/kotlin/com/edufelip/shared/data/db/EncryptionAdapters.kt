package com.edufelip.shared.data.db

import com.edufelip.shared.security.NoteCipher

internal fun encryptField(value: String): String = NoteCipher.encrypt(value)
internal fun decryptField(value: String): String = NoteCipher.decrypt(value)
