package com.emarsys.core.storage

interface TypedStorageApi<Value> {

    suspend fun put(key: String, value: Value)

    suspend fun get(key: String): Value
}
