package com.emarsys.core.storage

import com.emarsys.SdkConstants.UNKNOWN_WRAPPER_INFO
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.wrapper.WrapperInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

class WrapperInfoStorage(
    private val userDefaults: NSUserDefaults,
    private val sdkContext: SdkContextApi,
    private val sdkLogger: Logger,
    private val json: Json
): TypedStorageApi<WrapperInfo?> {
    override fun put(key: String, value: WrapperInfo?) {
        userDefaults.setObject(json.encodeToString(value), key)
    }

    override fun get(key: String): WrapperInfo? {
        return try {
            userDefaults.stringForKey(key)?.let {
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