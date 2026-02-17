package com.sap.ec.enable.config

import com.sap.ec.config.SdkConfig
import com.sap.ec.core.storage.StorageConstants
import com.sap.ec.core.storage.TypedStorageApi
import kotlinx.serialization.KSerializer

internal interface SdkConfigStoreApi<out LoadedType : SdkConfig> {
    val typedStorage: TypedStorageApi
    val deserializer: KSerializer<out LoadedType>

    suspend fun load(): LoadedType? {
        return typedStorage.get(StorageConstants.SDK_CONFIG_KEY, deserializer)
    }

    suspend fun store(config: SdkConfig)

    suspend fun clear()

}