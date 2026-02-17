package com.sap.ec.core.storage

import com.sap.ec.di.SdkKoinIsolationContext.koin
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class Store<Value>(
    private val stringStorage: StringStorageApi = koin.get<StringStorageApi>(),
    private val json: Json = koin.get<Json>(),
    private val key: String,
    private val serializer: KSerializer<Value>): ReadWriteProperty<Any?, Value?> {

    private var value: Value? = try {
        stringStorage.get(key)?.let {
            json.decodeFromString(serializer, it)
        }
    } catch (exception: Exception) {
        null
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Value? {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Value?) {
        if (value == null) {
            stringStorage.put(key, null)
        } else {
            stringStorage.put(key, json.encodeToString(serializer, value))
        }
        this.value = value
    }

}
