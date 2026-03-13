package com.sap.ec.api.embeddedmessaging

import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory
import kotlinx.serialization.Serializable

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("MessageCategory")
interface JsMessageCategory {
    val id: String
    val text: String
}

@Serializable
internal data class JSApiMessageCategory(
    override val id: String,
    override val text: String
) : JsMessageCategory

internal fun JsMessageCategory.toMessageCategory(): MessageCategory {
    return MessageCategory(id, text)
}

internal fun MessageCategory.toJsApiMessageCategory(): JSApiMessageCategory {
    return JSApiMessageCategory(id, text)
}