package com.sap.ec.enable.config

import com.sap.ec.api.config.AndroidEngagementCloudSDKConfig
import com.sap.ec.config.SdkConfig
import com.sap.ec.core.storage.StorageConstants
import com.sap.ec.core.storage.TypedStorageApi
import kotlinx.serialization.KSerializer

internal class AndroidSdkConfigStore(
    override val typedStorage: TypedStorageApi,
) : SdkConfigStoreApi<AndroidEngagementCloudSDKConfig> {
    override val deserializer: KSerializer<AndroidEngagementCloudSDKConfig> = AndroidEngagementCloudSDKConfig.serializer()

    override suspend fun store(config: SdkConfig) {
        typedStorage.put(
            StorageConstants.SDK_CONFIG_KEY,
            AndroidEngagementCloudSDKConfig.serializer(),
            config as AndroidEngagementCloudSDKConfig
        )
    }

    override suspend fun clear() {
        typedStorage.remove(StorageConstants.SDK_CONFIG_KEY)
    }
}