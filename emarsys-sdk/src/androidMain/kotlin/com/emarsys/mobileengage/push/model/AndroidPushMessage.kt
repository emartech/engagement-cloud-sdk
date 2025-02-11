package com.emarsys.mobileengage.push.model

import com.emarsys.mobileengage.push.PresentablePushData
import com.emarsys.mobileengage.push.PresentablePushMessage
import com.emarsys.mobileengage.push.PushData
import com.emarsys.mobileengage.push.SilentPushMessage
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


@Serializable(with = AndroidPushSerializer::class)
sealed interface AndroidPush

@Serializable
@SerialName("AndroidPushMessage")
data class AndroidPushMessage(
    override val messageId: String,
    override val title: String,
    override val body: String,
    override val iconUrlString: String? = null,
    override val imageUrlString: String? = null,
    override val data: PresentablePushData<AndroidPlatformData>,
) : AndroidPush, PresentablePushMessage<AndroidPlatformData>


@Serializable
@SerialName("AndroidSilentPushMessage")
data class AndroidSilentPushMessage(
    override val messageId: String,
    override val data: PushData<AndroidPlatformData>,
) : AndroidPush, SilentPushMessage<AndroidPlatformData>


object AndroidPushSerializer : KSerializer<AndroidPush> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("AndroidPush")

    override fun serialize(encoder: Encoder, value: AndroidPush) {
        when (value) {
            is AndroidPushMessage -> encoder.encodeSerializableValue(
                AndroidPushMessage.serializer(),
                value
            )

            is AndroidSilentPushMessage -> encoder.encodeSerializableValue(
                AndroidSilentPushMessage.serializer(),
                value
            )
        }
    }

    override fun deserialize(decoder: Decoder): AndroidPush {
        val input = decoder as? JsonDecoder ?: throw SerializationException("Expected JsonDecoder")
        val pushJson = input.decodeJsonElement().jsonObject
        val dataJson =
            pushJson["data"]?.jsonObject ?: throw SerializationException("Expected data object")

        return if (dataJson["silent"]?.jsonPrimitive?.boolean == true) {
            input.json.decodeFromJsonElement(AndroidSilentPushMessage.serializer(), pushJson)
        } else {
            input.json.decodeFromJsonElement(AndroidPushMessage.serializer(), pushJson)
        }
    }
}