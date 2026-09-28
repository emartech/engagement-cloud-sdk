package com.sap.ec.event

import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.recommendation.CartItem

class RecommendationEventPreProcessor(
    private val cartItemStorage: ThreadSafePersistentStoreApi<CartItem>
) : EventPreProcessorApi<SdkEvent> {
    override suspend fun process(event: SdkEvent) {
        if (event is SdkEvent.External.RecommendationTrackEvent.Cart) {
            cartItemStorage.setAll(event.items)
        }
    }
}
