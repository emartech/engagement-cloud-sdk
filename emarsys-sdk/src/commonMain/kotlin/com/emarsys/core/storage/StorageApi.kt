package com.emarsys.core.storage

interface StorageApi<Value> {

    fun put(key: String, value: Value?)

    fun get(key: String): Value?

}
