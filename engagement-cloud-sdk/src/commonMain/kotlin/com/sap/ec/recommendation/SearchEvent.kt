package com.sap.ec.recommendation

import com.sap.ec.api.event.model.TrackedEvent
import com.sap.ec.event.SdkEvent
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class SearchEvent(val searchTerm: String) : TrackedEvent {
    @OptIn(ExperimentalTime::class)
    override fun toSdkEvent(uuid: String, timestamp: Instant): Result<SdkEvent> =
        Result.success(
            SdkEvent.External.RecommendationTrackEvent.Search(
                searchTerm = searchTerm,
                id = uuid,
                timestamp = timestamp
            )
        )
}
