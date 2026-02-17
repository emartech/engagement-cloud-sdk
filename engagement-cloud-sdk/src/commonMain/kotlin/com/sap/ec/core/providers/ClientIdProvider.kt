package com.sap.ec.core.providers

import com.sap.ec.SdkConstants.CLIENT_ID_STORAGE_KEY
import com.sap.ec.core.storage.StringStorageApi

internal class ClientIdProvider(
    private val uuidProvider: UuidProviderApi,
    private val storage: StringStorageApi
) : Provider<String> {
    override fun provide(): String {
        return storage.get(CLIENT_ID_STORAGE_KEY) ?: generateClientId()
    }

    private fun generateClientId(): String {
        return uuidProvider.provide().also {
            storage.put(CLIENT_ID_STORAGE_KEY, it)
        }
    }
}