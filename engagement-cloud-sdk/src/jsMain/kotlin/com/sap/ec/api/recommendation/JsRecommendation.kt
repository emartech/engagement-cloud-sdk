package com.sap.ec.api.recommendation

import com.sap.ec.api.recommendation.model.JsRecommendationResult
import com.sap.ec.api.recommendation.model.toJsRecommendationResult
import com.sap.ec.event.SdkEvent

internal class JsRecommendation(
    private val recommendation: RecommendationApi
) : JsRecommendationApi {
    override suspend fun requestRecommendation(event: SdkEvent.Internal.Sdk.RequestRecommendation): JsRecommendationResult {
        return recommendation.requestRecommendation(
            event.options
        ).getOrThrow()
            .toJsRecommendationResult()
    }
}