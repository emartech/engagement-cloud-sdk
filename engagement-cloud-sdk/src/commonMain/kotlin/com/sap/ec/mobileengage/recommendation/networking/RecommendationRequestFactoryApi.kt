package com.sap.ec.mobileengage.recommendation.networking

import com.sap.ec.core.networking.model.UrlRequest
import com.sap.ec.event.RecommendationEvent

internal interface RecommendationRequestFactoryApi {
    suspend fun create(recommendationEvent: RecommendationEvent): UrlRequest
}