package com.sap.ec.api.recommendation

import com.sap.ec.api.recommendation.model.JsRecommendationResult
import com.sap.ec.api.recommendation.model.toJSRecommendationResult
import com.sap.ec.event.SdkEvent
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationLogic
import com.sap.ec.recommendation.models.requestRecommendation.RecommendationOptions

internal class JsRecommendation(
    private val recommendation: RecommendationApi
) : JsRecommendationApi {
    override suspend fun requestRecommendation(event: SdkEvent.Internal.Sdk.RequestRecommendation): Map<String, JsRecommendationResult> {
        return recommendation.requestRecommendation(
            SdkEvent.Internal.Sdk.RequestRecommendation(
                options = RecommendationOptions(
                    RecommendationLogic.Home
                )
            )
        ).getOrThrow()
            .mapValues { recommendationResultEntry ->
                recommendationResultEntry.value.toJSRecommendationResult()
            }
    }
}