package com.sap.ec.recommendation

import com.sap.ec.api.event.model.TrackedEvent
import com.sap.ec.event.SdkEvent
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class CartEvent(val items: List<CartItem>) : TrackedEvent {
    @OptIn(ExperimentalTime::class)
    override fun toSdkEvent(uuid: String, timestamp: Instant): Result<SdkEvent> =
        Result.success(
            SdkEvent.External.RecommendationEvent.Cart(
                items = items,
                id = uuid,
                timestamp = timestamp
            )
        )
}
