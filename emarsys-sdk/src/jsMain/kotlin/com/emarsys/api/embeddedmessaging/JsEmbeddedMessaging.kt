package com.emarsys.api.embeddedmessaging

import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory

class JsEmbeddedMessaging(private val embeddedMessagingApi: EmbeddedMessagingApi): JsEmbeddedMessagingApi {
    override val categories: List<MessageCategory>
        get() = embeddedMessagingApi.categories
}