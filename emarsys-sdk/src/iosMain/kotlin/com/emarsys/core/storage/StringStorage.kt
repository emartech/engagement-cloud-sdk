package com.emarsys.core.storage

class StringStorage: TypedStorageApi<String?> {
    override fun put(key: String, value: String?) {
    }

    override fun get(key: String): String? {
        return null
    }
}