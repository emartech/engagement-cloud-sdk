package com.sap.ec.networking.clients.embedded.messaging.serializer

import com.sap.ec.mobileengage.action.models.BasicActionModel
import com.sap.ec.mobileengage.action.models.PresentableActionModel
import com.sap.ec.networking.clients.embedded.messaging.model.Category
import com.sap.ec.networking.clients.embedded.messaging.model.EmbeddedMessage
import com.sap.ec.networking.clients.embedded.messaging.model.ListThumbnailImage
import com.sap.ec.networking.clients.embedded.messaging.model.MessagesResponse
import com.sap.ec.networking.clients.embedded.messaging.model.Meta
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object MessagesResponseSerializer : KSerializer<MessagesResponse> {
    @Serializable
    private data class EmbeddedMessageSurrogate(
        val id: String,
        val title: String,
        val lead: String,
        val listThumbnailImage: ListThumbnailImage?,
        val defaultAction: BasicActionModel?,
        val actions: List<PresentableActionModel>,
        val tags: List<String>,
        val categoryIds: List<String>,
        val receivedAt: Long,
        val expiresAt: Long?,
        val properties: Map<String, String>,
        val trackingInfo: String,
    )

    @Serializable
    private data class MessagesResponseSurrogate(
        val version: String,
        val top: Int,
        val meta: Meta,
        val messages: List<EmbeddedMessageSurrogate>,
    )

    override val descriptor: SerialDescriptor = MessagesResponseSurrogate.serializer().descriptor

    override fun serialize(encoder: Encoder, value: MessagesResponse) {
        val surrogate = MessagesResponseSurrogate(
            version = value.version,
            top = value.top,
            meta = value.meta,
            messages = value.messages.map { message ->
                EmbeddedMessageSurrogate(
                    id = message.id,
                    title = message.title,
                    lead = message.lead,
                    listThumbnailImage = message.listThumbnailImage,
                    defaultAction = message.defaultAction,
                    actions = message.actions,
                    tags = message.tags,
                    categoryIds = message.categories.map { it.id },
                    receivedAt = message.receivedAt,
                    expiresAt = message.expiresAt,
                    properties = message.properties,
                    trackingInfo = message.trackingInfo
                )
            }
        )
        encoder.encodeSerializableValue(MessagesResponseSurrogate.serializer(), surrogate)
    }

    override fun deserialize(decoder: Decoder): MessagesResponse {
        val surrogate = decoder.decodeSerializableValue(MessagesResponseSurrogate.serializer())
        val categoryMap = surrogate.meta.categories.associateBy { it.id }
        return MessagesResponse(
            version = surrogate.version,
            top = surrogate.top,
            meta = surrogate.meta,
            messages = surrogate.messages.map { messageSurrogate ->
                EmbeddedMessage(
                    id = messageSurrogate.id,
                    title = messageSurrogate.title,
                    lead = messageSurrogate.lead,
                    listThumbnailImage = messageSurrogate.listThumbnailImage,
                    defaultAction = messageSurrogate.defaultAction,
                    actions = messageSurrogate.actions,
                    tags = messageSurrogate.tags,
                    categories = messageSurrogate.categoryIds.mapNotNull { categoryId ->
                        val category = categoryMap[categoryId]
                        if (category == null) {
                            null
                        } else {
                            Category(category.id, category.value)
                        }
                    },
                    receivedAt = messageSurrogate.receivedAt,
                    expiresAt = messageSurrogate.expiresAt,
                    properties = messageSurrogate.properties,
                    trackingInfo = messageSurrogate.trackingInfo
                )
            }
        )
    }
}