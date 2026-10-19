package com.sap.ec.api.recommendation

import com.sap.ec.api.event.model.TrackedEvent
import com.sap.ec.recommendation.models.RecommendationResult
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions

class IosRecommendation(private val recommendationApi: RecommendationApi) : IosRecommendationApi {
    override suspend fun requestRecommendation(
        recommendationOptions: RecommendationOptions,
        contextEvent: TrackedEvent?
    ): RecommendationResult {
        return recommendationApi.requestRecommendation(recommendationOptions, contextEvent).getOrThrow()
    }
}