package com.sap.ec.api.embeddedmessaging

import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JsEmbeddedMessagingApi {

    val categories: List<MessageCategory>

}