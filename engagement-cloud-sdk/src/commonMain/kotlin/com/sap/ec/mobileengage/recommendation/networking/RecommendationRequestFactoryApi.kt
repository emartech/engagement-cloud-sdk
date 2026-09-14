package com.sap.ec.mobileengage.recommendation.networking

import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.event.SdkEvent

internal interface RecommendationRequestFactoryApi {
    suspend fun create(recommendationEvent: SdkEvent.External.RecommendationEvent): UrlRequest
}