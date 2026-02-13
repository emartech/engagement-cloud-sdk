package com.emarsys.mobileengage.inapp.view

import com.emarsys.mobileengage.inapp.InAppMessage
import com.emarsys.mobileengage.inapp.webview.WebViewHolder

interface InAppViewApi {
    val inAppMessage: InAppMessage
    suspend fun load(message: InAppMessage): WebViewHolder
}
