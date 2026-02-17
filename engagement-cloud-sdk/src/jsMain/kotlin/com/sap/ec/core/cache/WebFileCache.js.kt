package com.sap.ec.core.cache

class WebFileCache : FileCacheApi {
    override fun get(fileName: String): ByteArray? {
        return null
    }

    override fun cache(fileName: String, file: ByteArray) {}
    override fun remove(fileName: String) {}

}