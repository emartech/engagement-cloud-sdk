package com.emarsys.mobileengage.inapp

import io.ktor.http.Url

interface InlineInAppMessageFetcherApi {
    suspend fun fetch(viewId: String): InAppMessage?
    suspend fun fetch(url: Url): InAppMessage?
}