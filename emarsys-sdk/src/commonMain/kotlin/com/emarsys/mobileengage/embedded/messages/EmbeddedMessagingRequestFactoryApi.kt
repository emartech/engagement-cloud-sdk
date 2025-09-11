package com.emarsys.mobileengage.embedded.messages

import com.emarsys.core.networking.model.UrlRequest
import com.emarsys.event.SdkEvent

interface EmbeddedMessagingRequestFactoryApi {
    fun create(embeddedMessagingEvent: SdkEvent.Internal.EmbeddedMessaging): UrlRequest
}