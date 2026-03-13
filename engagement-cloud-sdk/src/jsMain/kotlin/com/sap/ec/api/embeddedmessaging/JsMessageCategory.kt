package com.sap.ec.api.embeddedmessaging

import com.sap.ec.networking.clients.embedded.messaging.model.Category
import kotlinx.serialization.Serializable

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("Category")
interface JsMessageCategory {
    val id: String
    val value: String
}

@Serializable
internal data class JSApiMessageCategory(
    override val id: String,
    override val value: String
) : JsMessageCategory

internal fun JsMessageCategory.toCategory(): Category {
    return Category(id, value)
}

internal fun Category.toJsApiCategory(): JSApiMessageCategory {
    return JSApiMessageCategory(id, text)
}