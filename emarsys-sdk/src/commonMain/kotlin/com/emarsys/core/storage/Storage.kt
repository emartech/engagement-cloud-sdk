package com.emarsys.core.storage

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Storage(val stringStorage: StorageApi<String?>, val json: Json) {
    inline fun <reified Value>put(key: String, value: Value) {
        val encodedValue = json.encodeToString(value)
        stringStorage.put(key, encodedValue)
    }

    inline fun <reified Value> get(key: String): Value? {
        val encodedValue = stringStorage.get(key)
        return encodedValue?.let { json.decodeFromString<Value?>(it) }
    }

}