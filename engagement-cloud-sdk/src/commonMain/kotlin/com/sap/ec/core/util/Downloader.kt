package com.sap.ec.core.util

import com.sap.ec.core.cache.FileCacheApi
import com.sap.ec.core.log.Logger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.readRawBytes
import io.ktor.http.Url
import io.ktor.http.isSuccess

internal class Downloader(
    private val client: HttpClient,
    private val fileCache: FileCacheApi,
    private val logger: Logger
) : DownloaderApi {

    override suspend fun download(urlString: String, fallback: ByteArray?): ByteArray? {
        return try {
            val url = Url(urlString)
            val fileName = "${urlString.hashCode()}"
            fileCache.get(fileName) ?: downloadAndCache(url, fileName) ?: fallback
        } catch (exception: Exception) {
            logger.error("Downloader", exception)
            fallback
        }
    }

    private suspend fun downloadAndCache(url: Url, fileName: String): ByteArray? {
        val response = client.get(url)
        return if (response.status.isSuccess()) {
            val result = response.readRawBytes()
            fileCache.cache(fileName, result)
            result
        } else {
            null
        }
    }
}