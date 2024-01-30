package com.emarsys.core.storage

interface TypedStorageApi<Value> {

    fun put(key: String, value: Value)

    fun get(key: String): Value

}
