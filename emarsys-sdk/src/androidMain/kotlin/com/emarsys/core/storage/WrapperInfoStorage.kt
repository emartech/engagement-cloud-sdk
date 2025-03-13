package com.emarsys.core.storage

import android.content.SharedPreferences
import androidx.core.content.edit
import com.emarsys.context.SdkContextApi
import com.emarsys.core.log.Logger
import com.emarsys.core.wrapper.WrapperInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class WrapperInfoStorage(
    private val sharedPreferences: SharedPreferences,
    private val sdkContext: SdkContextApi,
    private val sdkLogger: Logger,
    private val json: Json
) :
    TypedStorageApi<WrapperInfo?> {
        private companion object {
            const val UNKNOWN = "unknown"
        }
    override fun put(key: String, value: WrapperInfo?) {
        sharedPreferences.edit {
            putString(key, json.encodeToString(value))
        }
    }

    override fun get(key: String): WrapperInfo? {
        return try {
            sharedPreferences.getString(key, null)?.let {
                json.decodeFromString(it)
            }
        } catch (exception: SerializationException) {
            CoroutineScope(sdkContext.sdkDispatcher).launch {
                sdkLogger.error("WrapperInfoStorage - get", exception)
            }
            WrapperInfo(UNKNOWN, UNKNOWN)
        }
    }
}