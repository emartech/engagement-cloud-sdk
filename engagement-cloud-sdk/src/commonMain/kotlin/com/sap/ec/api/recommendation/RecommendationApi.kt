package com.sap.ec.api.recommendation

import com.sap.ec.api.AutoRegisterable
import com.sap.ec.recommendation.models.tracking.TrackedRecommendationEvent
import com.sap.ec.recommendation.models.RecommendationResult
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions

interface RecommendationApi : AutoRegisterable {
    suspend fun requestRecommendation(
        recommendationOptions: RecommendationOptions,
        contextEvent: TrackedRecommendationEvent? = null
    ): Result<RecommendationResult>
}