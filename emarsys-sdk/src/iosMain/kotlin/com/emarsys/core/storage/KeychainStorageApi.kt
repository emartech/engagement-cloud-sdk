package com.emarsys.core.storage

import platform.Foundation.NSData

internal interface KeychainStorageApi {
    fun readString(key: String): String?
    fun readData(key: String): NSData?
}
