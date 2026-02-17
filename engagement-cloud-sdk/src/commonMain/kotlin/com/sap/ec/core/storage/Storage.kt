package com.sap.ec.core.storage

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

class Storage(private val stringStorage: StringStorageApi, val json: Json): StorageApi {

    override fun <Value>put(key: String, serializer: KSerializer<Value>, value: Value?) {
        val encodedValue = value?.let {
            json.encodeToString(serializer, value)
        }
        stringStorage.put(key, encodedValue)
    }

    override fun <Value>get(key: String, serializer: KSerializer<Value>): Value? {
        val encodedValue = stringStorage.get(key)
        return if (!encodedValue.isNullOrEmpty()) {
            json.decodeFromString(serializer, encodedValue)
        } else {
            null
        }
    }

}
