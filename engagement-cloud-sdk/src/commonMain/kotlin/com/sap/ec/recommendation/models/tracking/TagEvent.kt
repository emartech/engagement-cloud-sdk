package com.sap.ec.recommendation.models.tracking

import com.sap.ec.event.SdkEvent
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class TagEvent(val tag: String, val attributes: Map<String, String>? = null) : TrackedRecommendationEvent {
    @OptIn(ExperimentalTime::class)
    override fun toSdkEvent(uuid: String, timestamp: Instant): Result<SdkEvent.External.RecommendationTrackEvent> =
        Result.success(
            SdkEvent.External.RecommendationTrackEvent.Tag(
                tag = tag,
                attributes = attributes,
                id = uuid,
                timestamp = timestamp
            )
        )
}
