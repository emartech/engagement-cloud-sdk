package com.sap.ec.api.event.model

import com.sap.ec.core.providers.TimestampProvider
import com.sap.ec.core.providers.UUIDProvider
import com.sap.ec.event.ExternalEventTypes
import com.sap.ec.util.toJsonObject
import com.sap.ec.util.toMap
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonClassDiscriminator
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("fullClassName")
sealed interface EngagementCloudEvent {
    val type: String
}

/**
 * Represents an event defined by the SAP Engagement Cloud platform user.
 *
 * @property id A unique identifier for the event.
 * @property name The name of the event.
 * @property attributes Additional attributes associated with the event.
 * @property timestamp The timestamp when the event occurred.
 * @property type The type of the event, which is always "app_event".
 */
@OptIn(ExperimentalObjCName::class)
@Serializable(with = AppEventSerializer::class)
@ObjCName("AppEvent")
data class AppEvent(
    val id: String = UUIDProvider().provide(),
    val name: String,
    val attributes: Map<String, Any>? = null,
    val timestamp: Instant = TimestampProvider().provide(),
    override val type: String = ExternalEventTypes.APP_EVENT.name.lowercase()
) : EngagementCloudEvent

/**
 * Represents a badge count event tracked by the SDK.
 *
 * This event is used to be notified about changes to the badge count,
 * typically for notifications or app icons.
 *
 * @property id A unique identifier for the event.
 * @property timestamp The timestamp when the event occurred.
 * @property badgeCount The badge count value that should be set or added
 * to the current badge count.
 * @property method The method used to update the badge count.
 * @property type The type of the event, which is always "badge_count".
 *
 * Possible values:
 * - SET
 * - ADD
 *
 */
@OptIn(ExperimentalObjCName::class)
@Serializable
@ObjCName("BadgeCount")
data class BadgeCountEvent(
    val id: String = UUIDProvider().provide(),
    val timestamp: Instant = TimestampProvider().provide(),
    val badgeCount: Int,
    val method: String,
    override val type: String = ExternalEventTypes.BADGE_COUNT.name.lowercase()
) : EngagementCloudEvent

private object AppEventSerializer : KSerializer<AppEvent> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("AppEvent") {
            element<String>("id")
            element<String>("name")
            element<JsonObject?>("attributes", isOptional = true)
            element<String>("timestamp")
            element<String>("type")
        }

    override fun deserialize(decoder: Decoder): AppEvent {
        val jsonObject = decoder.decodeSerializableValue(JsonObject.serializer())
        return AppEvent(
            id = jsonObject["id"]?.jsonPrimitive?.content!!,
            name = jsonObject["name"]?.jsonPrimitive?.content!!,
            attributes = jsonObject["attributes"]?.jsonObject?.toMap(),
            timestamp = Clock.System.now(),
            type = jsonObject["type"]?.jsonPrimitive?.content!!
        )
    }

    override fun serialize(encoder: Encoder, value: AppEvent) {
        val jsonObject = buildJsonObject {
            put("id", JsonPrimitive(value.id))
            put("name", JsonPrimitive(value.name))
            value.attributes?.let { attributes ->
                put("attributes", attributes.toJsonObject())
            }
            put("timestamp", JsonPrimitive(value.timestamp.toString()))
            put("type", JsonPrimitive(value.type))
        }
        encoder.encodeSerializableValue(JsonObject.serializer(), jsonObject)
    }
}

