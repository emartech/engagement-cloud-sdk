package com.sap.ec.api.event.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@Serializable(with = EventTypeSerializer::class)
@ObjCName("EngagementCloudEventType")
enum class EventType(val value: String) {
    APP_EVENT("app_event"),
    BADGE_COUNT("badge_count");

    companion object {
        fun fromValue(value: String): EventType {
            return entries.find { it.value == value }
                ?: throw IllegalArgumentException("Unknown EventType value: $value")
        }
    }
}

internal object EventTypeSerializer : KSerializer<EventType> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("EventType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: EventType) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): EventType {
        return EventType.fromValue(decoder.decodeString())
    }
}