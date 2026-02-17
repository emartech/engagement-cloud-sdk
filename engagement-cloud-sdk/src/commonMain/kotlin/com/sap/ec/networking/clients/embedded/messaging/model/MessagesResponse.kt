package com.sap.ec.networking.clients.embedded.messaging.model

import com.sap.ec.mobileengage.action.models.BasicActionModel
import com.sap.ec.mobileengage.action.models.PresentableActionModel
import com.sap.ec.networking.clients.embedded.messaging.serializer.MessagesResponseSerializer
import kotlinx.serialization.Serializable
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@Serializable(with = MessagesResponseSerializer::class)
data class MessagesResponse(
    val version: String,
    val top: Int,
    val meta: Meta,
    val messages: List<EmbeddedMessage>,
)

@Serializable
data class Meta(
    val categories: List<MessageCategory>,
)

@OptIn(ExperimentalJsExport::class)
@JsExport
@Serializable
data class MessageCategory(
    val id: Int,
    val value: String
)

@Serializable
data class EmbeddedMessage(
    val id: String,
    val title: String,
    val lead: String,
    val listThumbnailImage: ListThumbnailImage?,
    val defaultAction: BasicActionModel?,
    val actions: List<PresentableActionModel>,
    val tags: List<String>,
    val categories: List<Category>,
    val receivedAt: Long,
    val expiresAt: Long?,
    val properties: Map<String, String>,
    val trackingInfo: String,
)

@Serializable
data class ListThumbnailImage(
    val src: String,
    val alt: String?
)

@Serializable
data class Category(
    val id: Int,
    val text: String
)
