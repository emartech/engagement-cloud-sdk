package com.emarsys.core.storage

import com.emarsys.SdkConstants.UNKNOWN_WRAPPER_INFO
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.wrapper.WrapperInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.w3c.dom.Storage

class WrapperInfoStorage(
    private val storage: Storage,
    private val sdkContext: SdkContextApi,
    private val sdkLogger: Logger,
    private val json: Json
) : TypedStorageApi<WrapperInfo?> {
    override suspend fun put(key: String, value: WrapperInfo?) {
        value?.let {
            storage.setItem(key, json.encodeToString(it))
        } ?: storage.removeItem(key)
    }

    override suspend fun get(key: String): WrapperInfo? {
        return try {
            storage.getItem(key)?.let {
                json.decodeFromString(it)
            }
        } catch (exception: SerializationException) {
            CoroutineScope(sdkContext.sdkDispatcher).launch {
                sdkLogger.error("WrapperInfoStorage - get", exception)
            }
            WrapperInfo(UNKNOWN_WRAPPER_INFO, UNKNOWN_WRAPPER_INFO)
        }
    }
}