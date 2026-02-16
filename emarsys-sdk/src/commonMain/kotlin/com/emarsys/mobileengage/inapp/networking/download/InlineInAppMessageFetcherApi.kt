package com.emarsys.mobileengage.inapp.networking.download

import com.emarsys.mobileengage.inapp.InAppMessage
import io.ktor.http.Url

interface InlineInAppMessageFetcherApi {
    suspend fun fetch(viewId: String): InAppMessage?
    suspend fun fetch(url: Url, trackingInfo: String): InAppMessage?
}