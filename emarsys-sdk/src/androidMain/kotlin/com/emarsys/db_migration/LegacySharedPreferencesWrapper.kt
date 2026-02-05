package com.emarsys.db_migration

import android.content.SharedPreferences

class LegacySharedPreferencesWrapper(
    private val legacySharedPreferences: SharedPreferences,
    private val sharedPreferenceCrypto: SharedPreferenceCrypto
) {

    companion object Companion {
        const val EMARSYS_SECURE_SHARED_PREFERENCES_V3_NAME =
            "emarsys_secure_shared_preferences_v3"
    }

    suspend fun getDecryptedString(key: String): String? {
        val encryptedValue = legacySharedPreferences.getString(key, null)
        return encryptedValue?.let { sharedPreferenceCrypto.decrypt(it) }
    }
}
