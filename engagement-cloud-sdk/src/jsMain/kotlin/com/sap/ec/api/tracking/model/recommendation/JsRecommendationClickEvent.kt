package com.sap.ec.api.tracking.model.recommendation

import com.sap.ec.api.tracking.model.JsRecommendationEvent

@OptIn(ExperimentalJsExport::class)
@JsExport
@JsName("RecommendationClickEvent")
interface JsRecommendationClickEvent : JsRecommendationEvent {

    override val recommendationEventType: String
        get() = JsRecommendationEventType.RECOMMENDATION_CLICK.name

    val productId: String
}