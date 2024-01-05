package com.emarsys.core.device.fakes

import com.emarsys.core.storage.StorageApi

class FakeStorage: StorageApi<String> {
    private val storedValue: MutableMap<String, String?> = mutableMapOf()

    override fun put(key: String, value: String?) {
        storedValue[key] = value
    }

    override fun get(key: String): String? {
        return storedValue[key]
    }


}