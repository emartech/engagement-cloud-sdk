package com.sap.ec.api.embeddedmessaging

import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory

class JsEmbeddedMessaging(private val embeddedMessagingApi: EmbeddedMessagingApi): JsEmbeddedMessagingApi {
    override val categories: List<MessageCategory>
        get() = embeddedMessagingApi.categories
}