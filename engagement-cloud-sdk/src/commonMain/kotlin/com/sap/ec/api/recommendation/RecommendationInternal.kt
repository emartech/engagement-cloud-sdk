package com.sap.ec.api.recommendation

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.models.RecommendationResult

internal class RecommendationInternal(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val sdkLogger: Logger
) : RecommendationInstance {
    override suspend fun requestRecommendation(event: SdkEvent): Map<String, RecommendationResult> {
        val response =
            sdkEventDistributor.registerEvent(event).await<Map<String, RecommendationResult>>()
        sdkLogger.debug("RecommendationInternal - requestRecommendation")

        return response.result.getOrElse {
            sdkLogger.info("RecommendationInternal - requestRecommendation failed")
            mapOf()
        }
    }

    override suspend fun activate() {
        sdkLogger.debug("RecommendationInternal - activate")
    }
}