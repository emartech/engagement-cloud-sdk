package com.emarsys.mobileengage.inapp

interface InAppViewApi {
    val inAppMessage: InAppMessage
    suspend fun load(message: InAppMessage): WebViewHolder

}
