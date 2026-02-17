package com.sap.ec.mobileengage.inapp.iframe

import com.sap.ec.mobileengage.inapp.InAppMessage
import web.messaging.MessageChannel

interface MessageChannelProviderApi {
    fun provide(inAppMessage: InAppMessage): MessageChannel
}