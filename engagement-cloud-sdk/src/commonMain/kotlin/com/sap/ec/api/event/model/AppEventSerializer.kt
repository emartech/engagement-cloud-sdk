package com.sap.ec.api.event.model

import com.sap.ec.util.toJsonObject
import com.sap.ec.util.toMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive


internal object AppEventSerializer : KSerializer<AppEvent> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("AppEvent") {
            element<String>("id")
            element<String>("name")
            element<JsonObject?>("payload", isOptional = true)
            element<String?>("source", isOptional = true)
            element<String>("type")
        }

    override fun deserialize(decoder: Decoder): AppEvent {
        val jsonObject = decoder.decodeSerializableValue(JsonObject.Companion.serializer())
        return AppEvent(
            id = jsonObject["id"]?.jsonPrimitive?.content!!,
            name = jsonObject["name"]?.jsonPrimitive?.content!!,
            payload = jsonObject["payload"]?.jsonObject?.toMap(),
            source = jsonObject["source"]?.jsonPrimitive?.content?.let { sourceValue ->
                EventSource.entries.find { it.value == sourceValue }
            },
            type = EventType.fromValue(jsonObject["type"]?.jsonPrimitive?.content!!)
        )
    }

    override fun serialize(encoder: Encoder, value: AppEvent) {
        val jsonObject = buildJsonObject {
            put("id", JsonPrimitive(value.id))
            put("name", JsonPrimitive(value.name))
            value.payload?.let { attributes ->
                put("payload", attributes.toJsonObject())
            }
            value.source?.let { source ->
                put("source", JsonPrimitive(source.value))
            }
            put("type", JsonPrimitive(value.type.value))
        }
        encoder.encodeSerializableValue(JsonObject.Companion.serializer(), jsonObject)
    }
}
