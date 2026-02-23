package com.sap.ec.networking.clients.embedded.messaging.model

import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport


@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class MessageCategory(
    val id: Int,
    val value: String
)
