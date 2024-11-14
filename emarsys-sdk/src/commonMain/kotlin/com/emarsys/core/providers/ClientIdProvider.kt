package com.emarsys.core.providers

import com.emarsys.SdkConstants.CLIENT_ID_STORAGE_KEY
import com.emarsys.core.storage.TypedStorageApi

class ClientIdProvider(
    private val uuidProvider: Provider<String>,
    private val storage: TypedStorageApi<String?>
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