package com.sap.ec.api.recommendation.model

import com.sap.ec.recommendation.models.RecommendationResult

@OptIn(ExperimentalJsExport::class)
@JsName("RecommendationResult")
@JsExport
interface JsRecommendationResult {
    val recommendationsByFeature: Map<String, JsFeatureRecommendation>
}

fun RecommendationResult.toJsRecommendationResult(): JsRecommendationResult {
    return object : JsRecommendationResult {
        override val recommendationsByFeature =
            this@toJsRecommendationResult.recommendationsByFeature
                .mapValues { it.value.toJsFeatureRecommendation() }
    }
}
