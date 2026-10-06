package com.sap.ec.api.recommendation

import com.sap.ec.api.Activatable
import com.sap.ec.api.generic.GenericApi
import com.sap.ec.context.SdkContextApi
import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.models.product.Product
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
    override suspend fun requestRecommendation(event: SdkEvent): Result<List<Product>> = runCatchingWithoutCancellation {
        withContext(sdkContext.sdkDispatcher) {
            activeInstance<RecommendationInternalApi>().requestRecommendation(event)
        }
    }
}