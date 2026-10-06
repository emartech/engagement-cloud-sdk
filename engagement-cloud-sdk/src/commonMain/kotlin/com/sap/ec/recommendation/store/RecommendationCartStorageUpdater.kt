package com.sap.ec.recommendation.store

import com.sap.ec.core.collections.ThreadSafePersistentStoreApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.models.CartItem

class RecommendationCartStorageUpdater(
    private val cartItemStorage: ThreadSafePersistentStoreApi<CartItem>
) : RecommendationCartStorageUpdaterApi {
    override suspend fun updateFromEvent(event: SdkEvent.Internal.Sdk.RequestRecommendation) {
        event.contextEvent?.let {
            updateFromEvent(it)
        }
    }

    override suspend fun updateFromEvent(event: SdkEvent.External.RecommendationTrackEvent) {
        if (event is SdkEvent.External.RecommendationTrackEvent.Cart) {
            cartItemStorage.setAll(event.items)
        }
        if (event is SdkEvent.External.RecommendationTrackEvent.Purchase) {
            cartItemStorage.clear()
        }
    }
}
