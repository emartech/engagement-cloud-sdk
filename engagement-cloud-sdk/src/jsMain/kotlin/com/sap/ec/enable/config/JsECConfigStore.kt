package com.sap.ec.enable.config

import JsEngagementCloudSDKConfig
import com.sap.ec.config.SdkConfig
import com.sap.ec.core.storage.StorageConstants
import com.sap.ec.core.storage.TypedStorageApi
import kotlinx.serialization.KSerializer

internal class JsECConfigStore(
    override val typedStorage: TypedStorageApi
) : SdkConfigStoreApi<JsEngagementCloudSDKConfig> {
    override val deserializer: KSerializer<JsEngagementCloudSDKConfig> = JsEngagementCloudSDKConfig.serializer()

    override suspend fun store(config: SdkConfig) {
        typedStorage.put(
            StorageConstants.SDK_CONFIG_KEY,
            JsEngagementCloudSDKConfig.serializer(),
            config as JsEngagementCloudSDKConfig
        )
    }

    override suspend fun clear() {
        typedStorage.remove(StorageConstants.SDK_CONFIG_KEY)
    }
}