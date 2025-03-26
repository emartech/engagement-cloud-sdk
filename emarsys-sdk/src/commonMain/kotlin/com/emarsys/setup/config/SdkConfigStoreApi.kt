package com.emarsys.setup.config

import com.emarsys.SdkConfig
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.TypedStorageApi
import kotlinx.serialization.KSerializer

interface SdkConfigStoreApi<out LoadedType : SdkConfig> {
    val typedStorage: TypedStorageApi
    val deserializer: KSerializer<out LoadedType>

    suspend fun load(): LoadedType? {
        return typedStorage.get(StorageConstants.SDK_CONFIG_KEY, deserializer)
    }

    suspend fun store(config: SdkConfig)

}