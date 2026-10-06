package com.sap.ec.recommendation.store

import com.sap.ec.event.SdkEvent

internal interface RecommendationCartStorageUpdaterApi {
    suspend fun updateFromEvent(event: SdkEvent.Internal.Sdk.RequestRecommendation)
    suspend fun updateFromEvent(event: SdkEvent.External.RecommendationTrackEvent)
}