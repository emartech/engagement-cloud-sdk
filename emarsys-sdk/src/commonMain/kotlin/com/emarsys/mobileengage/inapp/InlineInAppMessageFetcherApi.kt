package com.emarsys.mobileengage.inapp

interface InlineInAppMessageFetcherApi {
    suspend fun fetch(viewId: String): InAppMessage?
}