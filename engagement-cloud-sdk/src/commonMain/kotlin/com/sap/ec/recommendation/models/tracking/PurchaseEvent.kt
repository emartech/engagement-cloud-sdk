package com.sap.ec.recommendation.models.tracking

import com.sap.ec.api.event.model.TrackedEvent
import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.models.CartItem
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class PurchaseEvent(val orderId: String, val items: List<CartItem>) : TrackedEvent {
    @OptIn(ExperimentalTime::class)
    override fun toSdkEvent(uuid: String, timestamp: Instant): Result<SdkEvent> {
        return if (items.isNotEmpty()) {
            Result.success(
                SdkEvent.External.RecommendationTrackEvent.Purchase(
                    orderId = orderId,
                    items = items,
                    id = uuid,
                    timestamp = timestamp
                )
            )
        } else {
            Result.failure(IllegalArgumentException("Cart items list cannot be empty!"))
        }
    }
}
