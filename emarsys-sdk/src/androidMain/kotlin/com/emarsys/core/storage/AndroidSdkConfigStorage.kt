package com.emarsys.core.storage

import com.emarsys.AndroidEmarsysConfig
import com.emarsys.SdkConfig
import com.emarsys.core.log.Logger
import kotlinx.serialization.json.Json

class AndroidSdkConfigStorage(
    private val stringStorage: TypedStorageApi<String?>,
    private val sdkLogger: Logger,
    private val json: Json
) : SuspendTypedStorageApi<SdkConfig?> {
    override suspend fun put(key: String, value: SdkConfig?) {
        try {
            stringStorage.put(key, json.encodeToString(value))
        } catch (exception: Exception) {
            sdkLogger.error("SdkConfigStorage - put", exception)
        }
    }

    override suspend fun get(key: String): SdkConfig? {
        return try {
            stringStorage.get(key)?.let {
                json.decodeFromString<AndroidEmarsysConfig>(it)
            }
        } catch (exception: Exception) {
            sdkLogger.error("SdkConfigStorage - get", exception)
            null
        }
    }
}
