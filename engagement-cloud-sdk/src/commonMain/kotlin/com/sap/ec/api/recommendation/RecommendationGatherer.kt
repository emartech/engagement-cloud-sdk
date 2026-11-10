package com.sap.ec.api.recommendation

import com.sap.ec.recommendation.models.tracking.TrackedRecommendationEvent
import com.sap.ec.core.exceptions.SdkException.RecommendationApiNotReadyException
import com.sap.ec.core.log.Logger
import com.sap.ec.recommendation.models.RecommendationResult
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions

internal class RecommendationGatherer(
    private val sdkLogger: Logger
) : RecommendationInstance {
    override suspend fun requestRecommendation(
        recommendationOptions: RecommendationOptions,
        contextEvent: TrackedRecommendationEvent?
    ): RecommendationResult {
        sdkLogger.debug("RecommendationGatherer - requestRecommendation called on gatherer instance")
        throw RecommendationApiNotReadyException()
    }

    override suspend fun activate() {
    }
}