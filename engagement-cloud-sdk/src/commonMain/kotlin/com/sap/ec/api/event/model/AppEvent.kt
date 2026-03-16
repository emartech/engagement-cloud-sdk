package com.sap.ec.api.event.model

import com.sap.ec.core.providers.UUIDProvider
import com.sap.ec.util.toJsonObject
import com.sap.ec.util.toMap
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
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
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Represents an event defined by the SAP Engagement Cloud platform user.
 *
 * @property id A unique identifier for the event.
 * @property name The name of the event.
 * @property payload Additional payload associated with the event.
 * @property type The type of the event, which is always "app_event".
 */
@OptIn(ExperimentalObjCName::class)
@Serializable(with = AppEventSerializer::class)
@ObjCName("AppEvent")
data class AppEvent(
    override val id: String = UUIDProvider().provide(),
    val name: String,
    val payload: Map<String, Any>? = null,
    override val type: EventType = EventType.APP_EVENT
) : EngagementCloudEvent()
