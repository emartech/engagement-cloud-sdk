package com.emarsys.setup.config

import com.emarsys.AndroidEmarsysConfig
import com.emarsys.SdkConfig
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.TypedStorageApi
import kotlinx.serialization.KSerializer

class AndroidSdkConfigStore(
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
}