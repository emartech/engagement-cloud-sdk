package com.emarsys.core.storage

import kotlinx.serialization.KSerializer

internal interface TypedStorageApi {

    suspend fun <Value>put(key: String, serializer: KSerializer<Value>, value: Value)

    suspend fun <Value>get(key: String, serializer: KSerializer<Value>): Value?

    suspend fun remove(key: String)

}
