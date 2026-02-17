package com.sap.ec.enable.config

import com.sap.ec.IosEngagementCloudSDKConfig
import com.sap.ec.config.SdkConfig
import com.sap.ec.core.storage.StorageConstants
import com.sap.ec.core.storage.TypedStorageApi
import kotlinx.serialization.KSerializer

internal class IosSdkConfigStore(
    override val typedStorage: TypedStorageApi
) : SdkConfigStoreApi<IosEngagementCloudSDKConfig> {
    override val deserializer: KSerializer<IosEngagementCloudSDKConfig> = IosEngagementCloudSDKConfig.serializer()

    override suspend fun store(config: SdkConfig) {
        typedStorage.put(
            StorageConstants.SDK_CONFIG_KEY,
            IosEngagementCloudSDKConfig.serializer(),
            config as IosEngagementCloudSDKConfig
        )
    }

    override suspend fun clear() {
        typedStorage.remove(StorageConstants.SDK_CONFIG_KEY)
    }
}