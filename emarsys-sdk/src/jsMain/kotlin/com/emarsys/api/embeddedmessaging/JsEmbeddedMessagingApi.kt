package com.emarsys.api.embeddedmessaging

import com.emarsys.networking.clients.embedded.messaging.model.MessageCategory

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JsEmbeddedMessagingApi {

    val categories: List<MessageCategory>

}