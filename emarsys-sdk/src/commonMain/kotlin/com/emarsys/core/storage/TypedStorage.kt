package com.emarsys.core.storage

import com.emarsys.core.log.Logger
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json


internal class TypedStorage(
    val stringStorage: StringStorageApi,
    val json: Json,
    val sdkLogger: Logger
) : TypedStorageApi {
    override suspend fun <Value> put(key: String, serializer: KSerializer<Value>, value: Value) {
        try {
            val stringValue = json.encodeToString(serializer, value)
            stringStorage.put(key, stringValue)
        } catch (exception: Exception) {
            sdkLogger.error("put", exception)
        }
    }

    override suspend fun <Value> get(key: String, serializer: KSerializer<Value>): Value? {
        return try {
            stringStorage.get(key)?.let {
                json.decodeFromString(serializer, it)
            }
        } catch (exception: Exception) {
            sdkLogger.error("get", exception)
            null
        }
    }

    override suspend fun remove(key: String) {
        stringStorage.put(key, null)
    }
}
