package com.sap.ec.api.recommendation

import com.sap.ec.api.event.model.TrackedEvent
import com.sap.ec.core.exceptions.SdkException.InvalidRecommendationConfigurationException
import com.sap.ec.core.exceptions.SdkException.MissingRecommendationConfigurationException
import com.sap.ec.core.exceptions.SdkException.RecommendationApiNotReadyException
import com.sap.ec.recommendation.models.RecommendationResult
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions
import io.ktor.utils.io.CancellationException

interface IosRecommendationApi {

    @Throws(
        CancellationException::class,
        RecommendationApiNotReadyException::class,
        MissingRecommendationConfigurationException::class,
        InvalidRecommendationConfigurationException::class
    )
    suspend fun requestRecommendation(recommendationOptions: RecommendationOptions, contextEvent: TrackedEvent? = null): RecommendationResult
}