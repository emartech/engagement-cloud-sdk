package com.emarsys.core.storage

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class Storage(private val stringStorage: TypedStorageApi<String?>, val json: Json): StorageApi {

    override fun <Value>put(key: String, serializer: KSerializer<Value>, value: Value?) {
        val encodedValue = value?.let {
            json.encodeToString(serializer, value)
        }
        stringStorage.put(key, encodedValue)
    }

    override fun <Value>get(key: String, serializer: KSerializer<Value>): Value? {
        val encodedValue = stringStorage.get(key)
        return if (encodedValue != null) {
            json.decodeFromString(serializer, encodedValue)
        } else {
            null
        }
    }

}
