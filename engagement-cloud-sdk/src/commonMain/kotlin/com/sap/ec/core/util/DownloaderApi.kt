package com.sap.ec.core.util

internal interface DownloaderApi {
    suspend fun download(urlString: String, fallback: ByteArray? = null): ByteArray?
}