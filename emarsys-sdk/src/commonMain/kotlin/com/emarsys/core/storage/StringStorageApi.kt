package com.emarsys.core.storage

interface StringStorageApi {

    fun put(key: String, value: String?)

    fun get(key: String): String?

}
