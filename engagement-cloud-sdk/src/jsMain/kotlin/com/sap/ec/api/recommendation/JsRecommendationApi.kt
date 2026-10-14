package com.sap.ec.api.recommendation

import com.sap.ec.api.recommendation.model.JsRecommendationResult
import com.sap.ec.event.SdkEvent

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JsRecommendationApi {
    suspend fun requestRecommendation(event: SdkEvent.Internal.Sdk.RequestRecommendation): Map<String, JsRecommendationResult>
}