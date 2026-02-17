package com.sap.ec.api.embeddedmessaging

import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory

interface IosEmbeddedMessagingApi {
    val categories: List<MessageCategory>
}