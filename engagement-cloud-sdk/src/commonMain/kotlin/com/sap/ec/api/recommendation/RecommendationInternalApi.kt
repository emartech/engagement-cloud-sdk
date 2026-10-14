package com.sap.ec.api.recommendation

import com.sap.ec.api.event.model.TrackedEvent
import com.sap.ec.recommendation.models.RecommendationResult
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions

internal interface RecommendationInternalApi {
    suspend fun requestRecommendation(
        recommendationOptions: RecommendationOptions,
        contextEvent: TrackedEvent? = null
    ): Map<String, RecommendationResult>
}