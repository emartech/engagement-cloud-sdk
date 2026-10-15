package com.sap.ec.api.recommendation

import com.sap.ec.api.Activatable
import com.sap.ec.api.event.model.TrackedEvent
import com.sap.ec.api.generic.GenericApi
import com.sap.ec.context.SdkContextApi
import com.sap.ec.recommendation.models.RecommendationResult
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions
import com.sap.ec.util.runCatchingWithoutCancellation
import kotlinx.coroutines.withContext

internal interface RecommendationInstance : RecommendationInternalApi, Activatable

internal class Recommendation<Logging : RecommendationInstance, Gatherer : RecommendationInstance, Internal : RecommendationInstance>(
    loggingApi: Logging,
    gathererApi: Gatherer,
    internalApi: Internal,
    sdkContext: SdkContextApi
) : GenericApi<Logging, Gatherer, Internal>(
    loggingApi, gathererApi, internalApi, sdkContext
), RecommendationApi {
    override suspend fun requestRecommendation(
        recommendationOptions: RecommendationOptions,
        contextEvent: TrackedEvent?
    ): Result<RecommendationResult> =
        runCatchingWithoutCancellation {
            withContext(sdkContext.sdkDispatcher) {
                activeInstance<RecommendationInternalApi>().requestRecommendation(
                    recommendationOptions = recommendationOptions,
                    contextEvent = contextEvent
                )
            }
        }
}