package com.emarsys.enable.config

import com.emarsys.IosEmarsysConfig
import com.emarsys.SdkConfig
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.TypedStorageApi
import kotlinx.serialization.KSerializer

internal class IosSdkConfigStore(
    override val typedStorage: TypedStorageApi
) : SdkConfigStoreApi<IosEmarsysConfig> {
    override val deserializer: KSerializer<IosEmarsysConfig> = IosEmarsysConfig.serializer()

    override suspend fun store(config: SdkConfig) {
        typedStorage.put(
            StorageConstants.SDK_CONFIG_KEY,
            IosEmarsysConfig.serializer(),
            config as IosEmarsysConfig
        )
    }

    override suspend fun clear() {
        typedStorage.remove(StorageConstants.SDK_CONFIG_KEY)
    }
}