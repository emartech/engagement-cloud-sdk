package com.sap.ec.api.event.model

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * Represents the origin that triggered.
 *
 * @property value The lowercase string representation used in JSON serialization.
 */
@OptIn(ExperimentalObjCName::class)
@ObjCName("EventSource")
enum class EventSource(val value: String) {
    /**
     * Event was triggered by a push notification interaction.
     */
    Push("push"),

    /**
     * Event was triggered by an in-app message interaction.
     */
    InApp("in_app"),

    /**
     * Event was triggered by an inline in-app message interaction.
     */
    InlineInApp("inline_in_app"),

    /**
     * Event was triggered by an embedded messaging rich content interaction.
     */
    EmbeddedMessagingRichContent("embedded_messaging_rich_content"),

    /**
     * Event was triggered by an on-event action.
     */
    OnEvent("on_event")
}
