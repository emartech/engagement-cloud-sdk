package com.emarsys.mobileengage.inapp.networking.download

import com.emarsys.mobileengage.inapp.InAppMessage

interface InAppDownloaderApi {
    suspend fun download(url: String): InAppMessage?
}