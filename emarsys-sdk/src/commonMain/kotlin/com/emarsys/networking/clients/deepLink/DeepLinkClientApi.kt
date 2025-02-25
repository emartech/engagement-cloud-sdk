package com.emarsys.networking.clients.deepLink

interface DeepLinkClientApi {

    suspend fun trackDeepLink(trackingId: String)
}