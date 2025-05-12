package com.emarsys.fake

import com.emarsys.core.storage.StringStorageApi

class FakeStringStorage: StringStorageApi {
    private val storage = mutableMapOf<String, String?>()

    override fun put(key: String, value: String?) {
        storage[key] = value
    }

    override fun get(key: String): String? {
        return storage[key]
    }
}