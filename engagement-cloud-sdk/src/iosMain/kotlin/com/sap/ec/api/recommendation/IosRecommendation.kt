package com.sap.ec.api.recommendation

import com.sap.ec.recommendation.models.tracking.TrackedRecommendationEvent
import com.sap.ec.recommendation.models.RecommendationResult
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions

class IosRecommendation(private val recommendationApi: RecommendationApi) : IosRecommendationApi {
    override suspend fun requestRecommendation(
        recommendationOptions: RecommendationOptions,
        contextEvent: TrackedRecommendationEvent?
    ): RecommendationResult {
        return recommendationApi.requestRecommendation(recommendationOptions, contextEvent).getOrThrow()
    }
}