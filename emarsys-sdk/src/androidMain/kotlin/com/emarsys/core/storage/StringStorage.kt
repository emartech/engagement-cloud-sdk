package com.emarsys.core.storage

import android.content.SharedPreferences
import androidx.core.content.edit

class StringStorage(private val sharedPreferences: SharedPreferences) : TypedStorageApi<String?> {

    override fun put(key: String, value: String?) {
        sharedPreferences.edit {
            putString(key, value)
        }
    }

    override fun get(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

}
