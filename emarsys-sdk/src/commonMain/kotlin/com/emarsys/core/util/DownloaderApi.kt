package com.emarsys.core.util

interface DownloaderApi {
    suspend fun download(urlString: String, fallback: ByteArray? = null): ByteArray?
}