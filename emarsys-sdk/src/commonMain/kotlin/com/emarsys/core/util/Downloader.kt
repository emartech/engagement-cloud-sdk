package com.emarsys.core.util

import com.emarsys.core.cache.FileCacheApi
import com.emarsys.core.log.Logger
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

    override suspend fun download(urlString: String): ByteArray? {
        val url = Url(urlString)
        val fileName = "${urlString.hashCode()}"

        val cachedFile = fileCache.get(fileName)
        return try {
            cachedFile ?: downloadAndCache(url, fileName)
        } catch (exception: Exception) {
            logger.error("Downloader", exception)
            null
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