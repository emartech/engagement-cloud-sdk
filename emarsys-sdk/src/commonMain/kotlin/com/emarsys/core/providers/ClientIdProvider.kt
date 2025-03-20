package com.emarsys.core.providers

import com.emarsys.SdkConstants.CLIENT_ID_STORAGE_KEY
import com.emarsys.core.storage.StringStorageApi

class ClientIdProvider(
    private val uuidProvider: Provider<String>,
    private val storage: StringStorageApi
): Provider<String> {
    override fun provide(): String {
        return storage.get(CLIENT_ID_STORAGE_KEY) ?: generateClientId()
    }

    private fun generateClientId(): String {
        return uuidProvider.provide().also {
            storage.put(CLIENT_ID_STORAGE_KEY, it)
        }
    }
}