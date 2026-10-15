package com.sap.ec.api.recommendation

import com.sap.ec.api.AutoRegisterable
import com.sap.ec.api.event.model.TrackedEvent
import com.sap.ec.recommendation.models.RecommendationResult
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions

internal interface RecommendationApi: AutoRegisterable {
    suspend fun requestRecommendation(
        recommendationOptions: RecommendationOptions,
        contextEvent: TrackedEvent? = null
    ): Result<RecommendationResult>
}