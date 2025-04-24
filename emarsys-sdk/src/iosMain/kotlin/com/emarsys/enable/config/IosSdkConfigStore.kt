package com.emarsys.enable.config

import com.emarsys.EmarsysConfig
import com.emarsys.SdkConfig
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.TypedStorageApi
import kotlinx.serialization.KSerializer

internal class IosSdkConfigStore(
    override val typedStorage: TypedStorageApi
) : SdkConfigStoreApi<EmarsysConfig> {
    override val deserializer: KSerializer<EmarsysConfig> = EmarsysConfig.serializer()

    override suspend fun store(config: SdkConfig) {
        typedStorage.put(
            StorageConstants.SDK_CONFIG_KEY,
            EmarsysConfig.serializer(),
            config as EmarsysConfig
        )
    }

    override suspend fun clear() {
        typedStorage.remove(StorageConstants.SDK_CONFIG_KEY)
    }
}