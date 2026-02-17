package com.sap.ec.mobileengage.inapp.view

import com.sap.ec.mobileengage.inapp.InAppMessage
import com.sap.ec.mobileengage.inapp.webview.WebViewHolder

interface InAppViewApi {
    val inAppMessage: InAppMessage
    suspend fun load(message: InAppMessage): WebViewHolder
}
