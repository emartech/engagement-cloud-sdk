package com.sap.ec.api.event.model

import com.sap.ec.core.providers.UUIDProvider
import kotlinx.serialization.Serializable
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Represents an event defined by the SAP Engagement Cloud platform user.
 *
 * @property id A unique identifier for the event.
 * @property name The name of the event.
 * @property payload Additional payload associated with the event.
 * @property source The origin that triggered the event, or null if unknown.
 * @property type The type of the event, which is always "app_event".
 */
@OptIn(ExperimentalObjCName::class)
@Serializable(with = AppEventSerializer::class)
@ObjCName("AppEvent")
data class AppEvent(
    override val id: String = UUIDProvider().provide(),
    val name: String,
    val payload: Map<String, Any>? = null,
    val source: EventSource? = null,
    override val type: EventType = EventType.APP_EVENT
) : EngagementCloudEvent()
