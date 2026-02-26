package com.sap.ec.api.embeddedmessaging

import com.sap.ec.networking.clients.embedded.messaging.model.MessageCategory
import kotlinx.serialization.Serializable

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("MessageCategory")
interface JsMessageCategory {
    val id: String
    val value: String
}

@Serializable
data class JSApiMessageCategory(
    override val id: String,
    override val value: String
) : JsMessageCategory

fun JsMessageCategory.toMessageCategory(): MessageCategory {
    return MessageCategory(id, value)
}

fun MessageCategory.toJsApiMessageCategory(): JSApiMessageCategory {
    return JSApiMessageCategory(id, value)
}