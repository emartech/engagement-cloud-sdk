package com.sap.ec.api.recommendation

import com.sap.ec.core.exceptions.SdkException.RecommendationApiNotReady
import com.sap.ec.core.log.Logger
import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.Product

internal class RecommendationGatherer(
    private val sdkLogger: Logger
) : RecommendationInstance {
    override suspend fun requestRecommendation(event: SdkEvent): List<Product> {
        sdkLogger.debug("RecommendationGatherer - requestRecommendation called on gatherer instance")
        throw RecommendationApiNotReady()
    }

    override suspend fun activate() {
    }
}