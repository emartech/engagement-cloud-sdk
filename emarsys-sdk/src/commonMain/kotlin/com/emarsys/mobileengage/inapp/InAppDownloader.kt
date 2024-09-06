package com.emarsys.mobileengage.inapp

import com.emarsys.core.util.DownloaderApi

class InAppDownloader(private val downloader: DownloaderApi): InAppDownloaderApi {

    override suspend fun download(url: String): String? {
        return downloader.download(url)?.decodeToString()
    }
}