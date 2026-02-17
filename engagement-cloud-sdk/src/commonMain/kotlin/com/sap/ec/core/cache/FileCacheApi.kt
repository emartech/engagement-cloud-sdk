package com.sap.ec.core.cache

interface FileCacheApi {

    fun get(fileName: String): ByteArray?

    fun cache(fileName: String, file: ByteArray)

    fun remove(fileName: String)
}