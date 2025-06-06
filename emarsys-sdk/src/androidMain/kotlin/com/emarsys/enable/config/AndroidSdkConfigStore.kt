package com.emarsys.enable.config

import com.emarsys.AndroidEmarsysConfig
import com.emarsys.config.SdkConfig
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.TypedStorageApi
import kotlinx.serialization.KSerializer

internal class AndroidSdkConfigStore(
    override val typedStorage: TypedStorageApi,
) : SdkConfigStoreApi<AndroidEmarsysConfig> {
    override val deserializer: KSerializer<AndroidEmarsysConfig> = AndroidEmarsysConfig.serializer()

    override suspend fun store(config: SdkConfig) {
        typedStorage.put(
            StorageConstants.SDK_CONFIG_KEY,
            AndroidEmarsysConfig.serializer(),
            config as AndroidEmarsysConfig
        )
    }

    override suspend fun clear() {
        typedStorage.remove(StorageConstants.SDK_CONFIG_KEY)
    }
}