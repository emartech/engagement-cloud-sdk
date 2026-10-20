package com.sap.ec.recommendation.models.tracking

import com.sap.ec.event.SdkEvent
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class CategoryViewEvent(val categoryPath: String) : TrackedRecommendationEvent {
    @OptIn(ExperimentalTime::class)
    override fun toSdkEvent(uuid: String, timestamp: Instant): Result<SdkEvent.External.RecommendationTrackEvent> =
        Result.success(
            SdkEvent.External.RecommendationTrackEvent.CategoryView(
                categoryPath = categoryPath,
                id = uuid,
                timestamp = timestamp
            )
        )
}
