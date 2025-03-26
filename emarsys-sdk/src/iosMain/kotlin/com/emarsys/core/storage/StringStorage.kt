package com.emarsys.core.storage

import com.emarsys.di.SdkComponent
import platform.Foundation.NSUserDefaults

internal class StringStorage(private val userDefaults: NSUserDefaults): StringStorageApi, SdkComponent {
    override fun put(key: String, value: String?) {
        userDefaults.setObject(value, key)
    }

    override fun get(key: String): String? {
        return userDefaults.stringForKey(key)
    }
}