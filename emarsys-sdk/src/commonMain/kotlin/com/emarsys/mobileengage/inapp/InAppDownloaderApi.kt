package com.emarsys.mobileengage.inapp

interface InAppDownloaderApi {
    suspend fun download(url: String): String?
}