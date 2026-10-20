package com.sap.ec.api.recommendation

import com.sap.ec.recommendation.models.tracking.TrackedRecommendationEvent
import com.sap.ec.recommendation.models.RecommendationResult
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions

internal interface RecommendationInternalApi {
    suspend fun requestRecommendation(
        recommendationOptions: RecommendationOptions,
        contextEvent: TrackedRecommendationEvent? = null
    ): RecommendationResult
}