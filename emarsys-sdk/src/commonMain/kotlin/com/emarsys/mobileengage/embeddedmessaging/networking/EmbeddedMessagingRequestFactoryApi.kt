package com.emarsys.mobileengage.embeddedmessaging.networking

import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.event.SdkEvent

interface EmbeddedMessagingRequestFactoryApi {
    fun create(embeddedMessagingEvent: SdkEvent.Internal.EmbeddedMessaging): UrlRequest
}