package com.sap.ec.mobileengage.inapp.networking.download

import com.sap.ec.mobileengage.inapp.InAppMessage
import io.ktor.http.Url

interface InlineInAppMessageFetcherApi {
    suspend fun fetch(viewId: String): InAppMessage?
    suspend fun fetch(url: Url, trackingInfo: String): InAppMessage?
}