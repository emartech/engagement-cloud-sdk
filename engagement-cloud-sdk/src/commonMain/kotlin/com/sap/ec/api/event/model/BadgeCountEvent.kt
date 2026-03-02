package com.sap.ec.api.event.model

import com.sap.ec.core.providers.UUIDProvider
import kotlinx.serialization.Serializable
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Represents a badge count event tracked by the SDK.
 *
 * This event is used to be notified about changes to the badge count,
 * typically for notifications or app icons.
 *
 * @property id A unique identifier for the event.
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
    val badgeCount: Int,
    val method: String,
    override val type: EventType = EventType.BADGE_COUNT
) : EngagementCloudEvent()