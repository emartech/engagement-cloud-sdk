package com.sap.ec.mobileengage.inapp.networking.download

import com.sap.ec.mobileengage.inapp.InAppMessage

interface InAppDownloaderApi {
    suspend fun download(url: String): InAppMessage?
}