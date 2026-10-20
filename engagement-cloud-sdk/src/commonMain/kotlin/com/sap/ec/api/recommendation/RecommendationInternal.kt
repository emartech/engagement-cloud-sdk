package com.sap.ec.api.recommendation

import com.sap.ec.recommendation.models.tracking.TrackedRecommendationEvent
import com.sap.ec.core.channel.SdkEventDistributorApi
import com.sap.ec.core.log.Logger
import com.sap.ec.core.providers.InstantProvider
import com.sap.ec.core.providers.UuidProviderApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.models.RecommendationResult
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions

internal class RecommendationInternal(
    private val sdkEventDistributor: SdkEventDistributorApi,
    private val uuidProvider: UuidProviderApi,
    private val timestampProvider: InstantProvider,
    private val sdkLogger: Logger
) : RecommendationInstance {
    override suspend fun requestRecommendation(
        recommendationOptions: RecommendationOptions,
        contextEvent: TrackedRecommendationEvent?
    ): RecommendationResult {
        val event = contextEvent?.toSdkEvent(
            uuidProvider.provide(),
            timestampProvider.provide()
        )?.getOrThrow()

        val response =
            sdkEventDistributor.registerEvent(
                SdkEvent.Internal.Sdk.RequestRecommendation(
                    options = recommendationOptions,
                    contextEvent = event
                )

            ).await<RecommendationResult>()
        sdkLogger.debug("RecommendationInternal - requestRecommendation")

        return response.result.getOrElse {
            sdkLogger.info("RecommendationInternal - requestRecommendation failed")
            RecommendationResult(emptyMap())
        }
    }

    override suspend fun activate() {
        sdkLogger.debug("RecommendationInternal - activate")
    }
}