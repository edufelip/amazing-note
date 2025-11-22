package com.edufelip.shared.security

import android.util.Base64
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.edufelip.shared.data.db.AndroidContextHolder
import java.security.SecureRandom

private const val PREF_FILE = "note_cipher_store"
private const val KEY_ALIAS = "note_cipher_key"

actual class SecureKeyStore actual constructor() {
    private val context get() = AndroidContextHolder.appContext

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREF_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    actual fun getOrCreateKey(): ByteArray {
        val stored = prefs.getString(KEY_ALIAS, null)
        if (stored != null) {
            return Base64.decode(stored, Base64.NO_WRAP)
        }
        val generated = generateKey()
        prefs.edit { putString(KEY_ALIAS, Base64.encodeToString(generated, Base64.NO_WRAP)) }
        return generated
    }
}

private fun generateKey(): ByteArray = ByteArray(32).apply {
    SecureRandom().nextBytes(this)
}
