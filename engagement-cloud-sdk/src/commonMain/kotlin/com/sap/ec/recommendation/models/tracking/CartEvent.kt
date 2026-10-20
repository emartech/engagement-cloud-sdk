package com.sap.ec.recommendation.models.tracking

import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.models.CartItem
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class CartEvent(val items: List<CartItem>) : TrackedRecommendationEvent {
    @OptIn(ExperimentalTime::class)
    override fun toSdkEvent(uuid: String, timestamp: Instant): Result<SdkEvent.External.RecommendationTrackEvent> =
        Result.success(
            SdkEvent.External.RecommendationTrackEvent.Cart(
                items = items,
                id = uuid,
                timestamp = timestamp
            )
        )
}