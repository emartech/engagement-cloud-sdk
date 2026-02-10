package com.emarsys.mobileengage.inapp.iframe

import com.emarsys.mobileengage.inapp.InAppMessage
import web.messaging.MessageChannel

interface MessageChannelProviderApi {
    fun provide(inAppMessage: InAppMessage): MessageChannel
}