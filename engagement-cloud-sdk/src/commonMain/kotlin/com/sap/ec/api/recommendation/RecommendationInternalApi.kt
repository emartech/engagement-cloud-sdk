package com.sap.ec.api.recommendation

import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.models.RecommendationResult

internal interface RecommendationInternalApi {
    suspend fun requestRecommendation(event: SdkEvent): Map<String, RecommendationResult>
}