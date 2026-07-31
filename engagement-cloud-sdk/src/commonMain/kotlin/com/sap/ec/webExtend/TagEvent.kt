package com.sap.ec.webExtend

import com.sap.ec.api.event.model.TrackedEvent
import com.sap.ec.event.SdkEvent
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class TagEvent(val tag: String, val attributes: Map<String, String>? = null) : TrackedEvent {
    @OptIn(ExperimentalTime::class)
    override fun toSdkEvent(uuid: String, timestamp: Instant): SdkEvent =
        SdkEvent.External.WebExtendEvent.Tag(tag = tag, attributes = attributes, id = uuid, timestamp = timestamp)
}
