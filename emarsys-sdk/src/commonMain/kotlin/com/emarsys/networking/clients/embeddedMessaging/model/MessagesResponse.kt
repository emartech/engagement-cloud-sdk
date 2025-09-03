package com.emarsys.networking.clients.embeddedMessaging.model

import com.emarsys.mobileengage.action.models.BasicActionModel
import com.emarsys.mobileengage.action.models.PresentableActionModel
import kotlinx.serialization.Serializable

@Serializable
data class MessagesResponse(
    val version: String,
    val count: Int,
    val top: Int,
    val meta: Meta,
    val messages: List<EmbeddedMessage>,

)

@Serializable
data class Meta(
    val categories: List<MessageCategory>,
)

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
    val imageUrl: String,
    val defaultAction: BasicActionModel,
    val actions : List<PresentableActionModel>,
    val tags: List<String>,
    val categoryIds: List<Int>,
    val receivedAt: Long,
    val expiresAt: Long,
    val properties: String,
    val trackingInfo: String,
)

