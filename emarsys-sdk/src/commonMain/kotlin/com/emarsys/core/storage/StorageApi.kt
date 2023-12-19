package com.emarsys.core.storage

interface StorageApi {

    fun put(key: String, value: String?)

    fun get(key: String): String?

}
