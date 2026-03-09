package com.sap.ec.mobileengage.inapp.networking.download

import com.sap.ec.mobileengage.inapp.InAppMessage

internal interface InAppDownloaderApi {
    suspend fun download(url: String): InAppMessage?
}