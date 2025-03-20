package com.emarsys.core.device.fakes

import com.emarsys.core.storage.TypedStorageApi

class FakeStorage: TypedStorageApi<String?> {
    private val storedValue: MutableMap<String, String?> = mutableMapOf()

    override suspend fun put(key: String, value: String?) {
        storedValue[key] = value
    }

    override suspend fun get(key: String): String? {
        return storedValue[key]
    }


}