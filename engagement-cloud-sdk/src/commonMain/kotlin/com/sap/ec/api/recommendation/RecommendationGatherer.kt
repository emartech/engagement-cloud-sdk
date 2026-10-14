package com.sap.ec.api.recommendation

import com.sap.ec.core.exceptions.SdkException.RecommendationApiNotReadyException
import com.sap.ec.core.log.Logger
import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.models.RecommendationResult

internal class RecommendationGatherer(
    private val sdkLogger: Logger
) : RecommendationInstance {
    override suspend fun requestRecommendation(event: SdkEvent): Map<String, RecommendationResult> {
        sdkLogger.debug("RecommendationGatherer - requestRecommendation called on gatherer instance")
        throw RecommendationApiNotReadyException()
    }

    override suspend fun activate() {
    }
}