package com.emarsys.core.storage

import android.content.SharedPreferences

class StringStorage(private val sharedPreferences: SharedPreferences): StorageApi<String?> {

    override fun put(key: String, value: String?) {
        sharedPreferences
            .edit()
            .putString(key, value)
            .apply()
    }

    override fun get(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

}
