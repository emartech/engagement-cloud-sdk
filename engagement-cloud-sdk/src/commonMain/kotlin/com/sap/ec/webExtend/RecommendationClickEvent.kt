package com.sap.ec.webExtend

import com.sap.ec.api.event.model.TrackedEvent
import com.sap.ec.event.SdkEvent
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class RecommendationClickEvent(val productId: String) : TrackedEvent {
    @OptIn(ExperimentalTime::class)
    override fun toSdkEvent(uuid: String, timestamp: Instant): SdkEvent =
        SdkEvent.External.WebExtendEvent.RecommendationClick(productId = productId, feature = "", cohort = "", id = uuid, timestamp = timestamp)
}
