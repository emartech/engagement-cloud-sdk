package com.sap.ec.api.recommendation

import com.sap.ec.api.recommendation.model.JsRecommendationOptions
import com.sap.ec.api.recommendation.model.JsRecommendationResult
import com.sap.ec.api.tracking.model.JsRecommendationEvent

@OptIn(ExperimentalJsExport::class)
@JsExport
interface JsRecommendationApi {
    suspend fun requestRecommendation(
        recommendationOptions: JsRecommendationOptions,
        contextEvent: JsRecommendationEvent? = null
    ): JsRecommendationResult
}