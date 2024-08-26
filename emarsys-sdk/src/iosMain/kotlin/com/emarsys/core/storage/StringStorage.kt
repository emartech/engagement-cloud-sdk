package com.emarsys.core.storage

import platform.Foundation.NSUserDefaults

class StringStorage(private val userDefaults: NSUserDefaults): TypedStorageApi<String?> {
    override fun put(key: String, value: String?) {
        userDefaults.setObject(value, key)
    }

    override fun get(key: String): String? {
        return userDefaults.stringForKey(key)
    }
}