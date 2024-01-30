package com.emarsys.core.storage

import kotlinx.serialization.KSerializer

interface StorageApi {

    fun <Value>put(key: String, serializer: KSerializer<Value>, value: Value?)

    fun <Value>get(key: String, serializer: KSerializer<Value>): Value?

}