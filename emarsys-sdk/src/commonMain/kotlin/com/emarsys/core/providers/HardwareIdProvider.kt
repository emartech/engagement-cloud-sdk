package com.emarsys.core.providers

import com.emarsys.SdkConstants.HARDWARE_ID_STORAGE_KEY
import com.emarsys.core.storage.TypedStorageApi

class HardwareIdProvider(
    private val uuidProvider: Provider<String>,
    private val storage: TypedStorageApi<String?>
): Provider<String> {
    override fun provide(): String {
        return storage.get(HARDWARE_ID_STORAGE_KEY) ?: generateHardwareId()
    }

    private fun generateHardwareId(): String {
        return uuidProvider.provide().also {
            storage.put(HARDWARE_ID_STORAGE_KEY, it)
        }
    }
}