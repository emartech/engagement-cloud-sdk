package com.sap.ec.api.recommendation

import com.sap.ec.api.event.model.TrackedEvent
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
        contextEvent: TrackedEvent?
    ): Map<String, RecommendationResult> {
        val event = contextEvent?.let {
            it.toSdkEvent(
                uuidProvider.provide(),
                timestampProvider.provide()
            ).getOrThrow() as SdkEvent.External.RecommendationTrackEvent
        }

        val response =
            sdkEventDistributor.registerEvent(
                SdkEvent.Internal.Sdk.RequestRecommendation(
                    options = recommendationOptions,
                    contextEvent = event
                )

            ).await<Map<String, RecommendationResult>>()
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