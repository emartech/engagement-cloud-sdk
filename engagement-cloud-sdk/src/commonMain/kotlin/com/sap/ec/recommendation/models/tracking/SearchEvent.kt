package com.sap.ec.recommendation.models.tracking

import com.sap.ec.event.SdkEvent
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class SearchEvent(val searchTerm: String) : TrackedRecommendationEvent {
    @OptIn(ExperimentalTime::class)
    override fun toSdkEvent(uuid: String, timestamp: Instant): Result<SdkEvent.External.RecommendationTrackEvent> =
        Result.success(
            SdkEvent.External.RecommendationTrackEvent.Search(
                searchTerm = searchTerm,
                id = uuid,
                timestamp = timestamp
            )
        )
}
