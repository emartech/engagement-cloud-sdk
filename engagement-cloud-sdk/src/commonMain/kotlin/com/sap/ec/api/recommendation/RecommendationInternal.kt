package com.sap.ec.api.recommendation

import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.Product

internal class RecommendationInternal(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val sdkLogger: Logger
) : RecommendationInstance {
    override suspend fun requestRecommendation(event: SdkEvent): List<Product> {
        val response = sdkEventDistributor.registerEvent(event).await<List<Product>>()
        sdkLogger.debug("RecommendationInternal - requestRecommendation")

        return response.result.getOrElse {
            sdkLogger.info("RecommendationInternal - requestRecommendation failed")
            emptyList()
        }
    }

    override suspend fun activate() {
        sdkLogger.debug("RecommendationInternal - activate")
    }
}