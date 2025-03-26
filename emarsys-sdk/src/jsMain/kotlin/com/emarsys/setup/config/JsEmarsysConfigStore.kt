package com.emarsys.setup.config

import com.emarsys.JsEmarsysConfig
import com.emarsys.SdkConfig
import com.emarsys.core.storage.StorageConstants
import com.emarsys.core.storage.TypedStorageApi
import kotlinx.serialization.KSerializer

class JsEmarsysConfigStore(
    override val typedStorage: TypedStorageApi
) : SdkConfigStoreApi<JsEmarsysConfig> {
    override val deserializer: KSerializer<JsEmarsysConfig> = JsEmarsysConfig.serializer()

    override suspend fun store(config: SdkConfig) {
        typedStorage.put(
            StorageConstants.SDK_CONFIG_KEY,
            JsEmarsysConfig.serializer(),
            config as JsEmarsysConfig
        )
    }
}